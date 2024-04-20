import getPodcastFromFeedFunction, { Episode, Podcast } from 'podparse';
import HttpClient from '../http-client.js';
import PodcastDataStorage from './podcast-data-storage.js';

interface PodcastHelpersInterface {
    readonly getPodcastFromFeed: typeof getPodcastFromFeedFunction;
    readonly httpClient: HttpClient;
    readonly podcastDataStorage: PodcastDataStorage;

    getPodcastFromUrl(url: string): Promise<Podcast>;
    getMostRecentPodcastEpisode(podcast: Podcast): Episode;
    mostRecentPodcastEpisodeIsNew(podcast: Podcast): boolean;
}

export default class PodcastHelpers implements PodcastHelpersInterface {
    constructor(
        readonly getPodcastFromFeed: typeof getPodcastFromFeedFunction,
        readonly httpClient: HttpClient,
        readonly podcastDataStorage: PodcastDataStorage,
    ) {}

    public async getPodcastFromUrl(feedUrl: string): Promise<Podcast> {
        const response = await this.httpClient.get(feedUrl, 5000);
        const podcast = this.getPodcastFromFeed(response?.data || '');
        // meta.importFeedUrl is only officially supported in the SoundOn Namespace, but I find it super useful so I'm using it.
        podcast.meta.importFeedUrl = feedUrl;

        return podcast;
    }

    public getMostRecentPodcastEpisode(podcast: Podcast): Episode {
        podcast.episodes.sort((a, b) => {
            return new Date(b.pubDate).getTime() - new Date(a.pubDate).getTime();
        });

        const mostRecentEpisode = podcast?.episodes?.[0];
        if (!mostRecentEpisode) {
            throw new Error('Error finding most recent episode.');
        }
        return mostRecentEpisode;
    }

    public mostRecentPodcastEpisodeIsNew(podcast: Podcast): boolean {
        const mostRecentEpisode = this.getMostRecentPodcastEpisode(podcast);
        const guidList = this.podcastDataStorage.getPostedByFeedUrl(
            podcast.meta.importFeedUrl || '',
        );

        return !guidList.includes(mostRecentEpisode.guid);
    }
}

import { Logger } from 'log4js';
import { performance } from 'perf_hooks';
import getPodcastFromFeedFunction, { Episode, Podcast } from 'podparse';
import * as Constants from '../../constants.js';
import PodcastDataStorage from './podcast-data-storage.js';
import PodcastFetch from './podcast-fetch.js';

interface PodcastHelpersInterface {
    readonly constants: typeof Constants;
    readonly getPodcastFromFeed: typeof getPodcastFromFeedFunction;
    readonly logger: Logger;
    readonly podcastDataStorage: PodcastDataStorage;
    readonly podcastFetch: PodcastFetch;

    getPodcastFromUrl(url: string): Promise<Podcast>;
    getMostRecentPodcastEpisode(podcast: Podcast): Episode;
    mostRecentPodcastEpisodeIsNew(podcast: Podcast): boolean;
}

export default class PodcastHelpers implements PodcastHelpersInterface {
    constructor(
        readonly constants: typeof Constants,
        readonly getPodcastFromFeed: typeof getPodcastFromFeedFunction,
        readonly logger: Logger,
        readonly podcastDataStorage: PodcastDataStorage,
        readonly podcastFetch: PodcastFetch,
    ) {}

    public async getPodcastFromUrl(feedUrl: string): Promise<Podcast> {
        this.logger.debug('Podcast fetch starting', { feedUrl });
        const start = performance.now();

        let xmlString = '';
        try {
            xmlString = await this.podcastFetch.getPartialPodcastStringFromUrl(feedUrl, 5000);
        } catch (error) {
            this.logger.debug('Podcast fetch time-out', { feedUrl, error });
        }

        const end = performance.now();
        this.logger.debug('Podcast fetch finished', { feedUrl, timeElapsed: end - start });

        // If the returned string is falsey don't try to parse it.
        if (!xmlString) {
            throw new Error('Nothing returned from podcast fetch method.');
        }

        const podcast = this.getPodcastFromFeed(xmlString);
        // meta.importFeedUrl is only officially supported in the SoundOn Namespace, but I find it super useful
        // so I'm using it.
        podcast.meta.importFeedUrl = feedUrl;

        return podcast;
    }

    public getMostRecentPodcastEpisode(podcast: Podcast): Episode {
        if (podcast.episodes.length < 1) {
            throw new Error(this.constants.ERRORS.NO_PODCAST_EPISODES_FOUND_MESSAGE);
        }

        // Sort so most recent pubDate becomes first (index zero)
        podcast.episodes.sort((a, b) => {
            return new Date(b.pubDate).getTime() - new Date(a.pubDate).getTime();
        });

        return podcast?.episodes?.[0];
    }

    public mostRecentPodcastEpisodeIsNew(podcast: Podcast): boolean {
        const mostRecentEpisode = this.getMostRecentPodcastEpisode(podcast);
        const guidList = this.podcastDataStorage.getPostedByFeedUrl(
            podcast.meta.importFeedUrl || '',
        );

        return !guidList.includes(mostRecentEpisode.guid);
    }
}

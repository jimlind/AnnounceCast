import { RESOLVER } from 'awilix';
import { PodcastEpisode } from '../../models/podcast-episode.js';
import { Podcast } from '../../models/podcast.js';
import { PodcastDataStorage } from './podcast-data-storage.js';

export class PodcastHelpers {
    static [RESOLVER] = {};

    podcastDataStorage: PodcastDataStorage;

    constructor(podcastDataStorage: PodcastDataStorage) {
        this.podcastDataStorage = podcastDataStorage;
    }

    podcastHasLatestEpisode(podcast: Podcast): boolean {
        const guid = this.podcastDataStorage.getPostedFromUrl(podcast.feed);
        return podcast.episodeList.reduce((accumulator: boolean, current: PodcastEpisode) => {
            return accumulator || current.guid == guid;
        }, false);
    }
}

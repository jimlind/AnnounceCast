import { RESOLVER } from 'awilix';
import { AxiosInstance, AxiosResponse } from 'axios';
import { Podcast } from '../../models/podcast.js';
import { PodcastRssProcessor } from './podcast-rss-processor.js';

// JSON parsing creates some generics, so having a class here to help sort that out
type Dictionary = {
    [key: string]: string;
};

export class PodcastAppleAPIProcessor {
    static [RESOLVER] = {};

    axios: AxiosInstance;
    podcastRssProcessor: PodcastRssProcessor;

    constructor(axios: AxiosInstance, podcastRssProcessor: PodcastRssProcessor) {
        this.axios = axios;
        this.podcastRssProcessor = podcastRssProcessor;
    }

    search(searchTerm: string, podcastCount: number): Promise<Podcast[]> {
        return new Promise((resolve, reject) => {
            const url = `https://itunes.apple.com/search?term=${searchTerm}&country=US&media=podcast&attribute=titleTerm&limit=${podcastCount}`;
            this.axios.get(url).then((response: AxiosResponse) => {
                const promiseList = response.data.results.map((result: Dictionary) => {
                    return this.podcastRssProcessor.process(result.feedUrl, 0);
                });
                Promise.all(promiseList).then((results) => {
                    // Yuck. Type guard functions are ugly.
                    const isPodcast = (item: any): item is Podcast => item instanceof Podcast;
                    return resolve(results.filter(isPodcast));
                });
            });
        });
    }
}

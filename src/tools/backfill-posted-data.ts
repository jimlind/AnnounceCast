import { Container } from '../container.js';
import { Podcast } from '../models/podcast.js';
import { PodcastDataStorage } from '../services/podcast/podcast-data-storage.js';
import { PodcastRssProcessor } from '../services/podcast/podcast-rss-processor.js';

const container: Container = new Container('dev');
container.register().then(() => {
    const data = container.resolve<PodcastDataStorage>('podcastDataStorage');
    const feedUrlList = data.getPostedFeeds();

    for (var x = 0; x < feedUrlList.length; x++) {
        processFeed(data, feedUrlList[x]);
    }
});

function processFeed(data: PodcastDataStorage, feedUrl: string) {
    const podcastPromise = container
        .resolve<PodcastRssProcessor>('podcastRssProcessor')
        .process(feedUrl, 5)
        .catch(() => feedUrl);

    podcastPromise.then((result) => {
        if (!(result instanceof Podcast)) {
            return;
        }

        const guidList = result.episodeList.reverse().map((podcast) => podcast.guid);
        processData(data, result.feed, guidList);
    });
}

function processData(data: PodcastDataStorage, feedUrl: string, guidList: string[]) {
    const storedGuidList = data.getPostedFromUrl(feedUrl);
    const storedLastGuid = storedGuidList[storedGuidList.length - 1];
    const foundLastGuid = guidList[guidList.length - 1];

    if (storedLastGuid === foundLastGuid) {
        for (var x = 0; x < guidList.length; x++) {
            data.updatePostedData(feedUrl, guidList[x]);
        }
    } else {
        console.log(
            `Error attempting to backfill ${feedUrl}. Likely this means the feed isn't a real feed.`,
        );
    }
}

import * as Chrono from 'chrono-node';
import { Logger } from 'log4js';

interface PodcastFetchInterface {
    readonly logger: Logger;
    getPartialPodcastStringFromUrl(input: string, timeout: number): Promise<string>;
}

export default class PodcastFetch implements PodcastFetchInterface {
    constructor(
        readonly chrono: typeof Chrono,
        readonly logger: Logger,
    ) {}

    public async getPartialPodcastStringFromUrl(feedUrl: string, timeout: number): Promise<string> {
        // Set up the abort controller and start the timer
        const controller = new AbortController();
        const timeoutId = setTimeout(() => {
            controller.abort();
        }, timeout);

        // Fetch the feed to start getting response data
        const response = await fetch(feedUrl, {
            headers: { 'User-Agent': 'AnnounceCast Cient' },
            signal: controller.signal,
        });

        // If the response doesn't have an OK success message exit early
        if (response.status !== 200) {
            return '';
        }

        // If we can't setup a stream reader exit early
        const streamReader = response.body?.getReader();
        if (!streamReader) {
            return '';
        }

        let resultString = '';
        let channelTags = '';
        let latestItem = { pub: 0, item: '' };
        const itemRegexOpenAndClose = /<item.*?>[\s\S]+?<\/item>/g;
        const itemRegexOpen = /<item.*?>/;
        const pubDateRegexInner = /<pubDate>(.+?)<\/pubDate>/;

        // Start reading chunks of data
        const decoder = new TextDecoder();
        let resultChunk = await streamReader.read();
        while (!resultChunk.done) {
            // Build the most full return string
            resultString += decoder.decode(resultChunk.value);

            // Set the channel tags if they aren't already
            if (!channelTags) {
                const firstItemPosition = resultString.search(itemRegexOpen);
                if (firstItemPosition > 0) {
                    channelTags = resultString.substring(0, firstItemPosition);
                    // Remove checked data. Keep the resultString small.
                    resultString = resultString.substring(firstItemPosition);
                }
            }

            // Set the most latest item
            const itemList = resultString.match(itemRegexOpenAndClose) || [];
            for (const item of itemList) {
                const pubDate = item.match(pubDateRegexInner) || [];
                const pubInt = this.chrono.parseDate(pubDate[1])?.getTime() || 0;

                // We found a newer item. Set the latestItem and continue.
                // Explicitly using "<" under the assumption that if there are multiple items with same pubsub that we
                // want to use the first one.
                if (latestItem.pub < pubInt) {
                    latestItem = { pub: pubInt, item: item };
                }

                // We found an older item. We won't find any new items so exit.
                // Explicitly using ">="" under the assumption that if there are multiple items with same pubsub that we
                // should abort and return the first one.
                if (latestItem.pub >= pubInt) {
                    controller.abort();
                    return channelTags + latestItem.item + '</channel></rss>';
                }
            }

            // Remove checked data. Keep the resultString small.
            resultString = resultString.replace(itemRegexOpenAndClose, '');
            // Read the next chunk
            resultChunk = await streamReader.read();
        }

        // Stop the timer. Just for some odd edgecase possibilities.
        clearTimeout(timeoutId);
        return channelTags + latestItem.item + '</channel></rss>';
    }
}

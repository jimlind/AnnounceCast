interface PodcastFetchInterface {
    getPartialPodcastStringFromUrl(input: string, timeout: number): Promise<string>;
}

export default class PodcastFetch implements PodcastFetchInterface {
    public async getPartialPodcastStringFromUrl(feedUrl: string, timeout: number): Promise<string> {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => {
            controller.abort();
        }, timeout);

        const response = await fetch(feedUrl, {
            headers: { 'User-Agent': 'AnnounceCast Cient' },
            signal: controller.signal,
        });

        const streamReader = response.body?.getReader();
        if (!streamReader) {
            return '';
        }

        const decoder = new TextDecoder();
        let resultString = '';
        let resultChunk = await streamReader.read();
        while (!resultChunk.done) {
            resultString += decoder.decode(resultChunk.value);

            if (this.firstPubDatesAreSequential(resultString)) {
                clearTimeout(timeoutId);

                // This feels a little sketchy to abort the controller and return but it works.
                controller.abort();
                return this.cleanPartialPodcastFeed(resultString);
            }

            resultChunk = await streamReader.read();
        }
        clearTimeout(timeoutId);

        return resultString;
    }

    private firstPubDatesAreSequential(input: string) {
        const pubDates = [...input.matchAll(/<item>[\s\S]+?<pubDate>(.+?)<\/pubDate>/g)];
        if (pubDates.length < 2) {
            return false;
        }

        const isSequential =
            new Date(pubDates[0][1]).getTime() > new Date(pubDates[1][1]).getTime();
        const secondPubDateisClosed = input.indexOf('</item>', pubDates[1]['index']) > 0;

        return isSequential && secondPubDateisClosed;
    }

    private cleanPartialPodcastFeed(input: string) {
        const lastItemClose = input.lastIndexOf('</item>');
        const unclosedItemString = input.substring(0, lastItemClose);

        return unclosedItemString + '</item></channel></rss>';
    }
}

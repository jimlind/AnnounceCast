import { Logger } from 'log4js';

interface PodcastFetchInterface {
    readonly logger: Logger;
    getPartialPodcastStringFromUrl(input: string, timeout: number): Promise<string>;
}

export default class PodcastFetch implements PodcastFetchInterface {
    constructor(readonly logger: Logger) {}

    public async getPartialPodcastStringFromUrl(feedUrl: string, timeout: number): Promise<string> {
        const isProblematicPodcast = feedUrl.includes('meinpodcast');

        const controller = new AbortController();
        const timeoutId = setTimeout(() => {
            if (isProblematicPodcast) {
                this.logger.debug('ProblematicPodcast Timeout Happened');
            }
            controller.abort();
        }, timeout);

        if (isProblematicPodcast) {
            this.logger.debug('ProblematicPodcast Fetch Started');
        }

        const response = await fetch(feedUrl, {
            headers: { 'User-Agent': 'AnnounceCast Cient' },
            signal: controller.signal,
        });

        if (isProblematicPodcast) {
            this.logger.debug('ProblematicPodcast Fetch Completed', { response });
        }

        // If the response doesn't have an OK success message exit early
        if (response.status !== 200) {
            if (isProblematicPodcast) {
                this.logger.debug('ProblematicPodcast Response is not OK', {
                    status: response.status,
                });
            }
            return '';
        }

        const streamReader = response.body?.getReader();
        if (!streamReader) {
            if (isProblematicPodcast) {
                this.logger.debug('ProblematicPodcast Response can not get reader', {
                    streamReader,
                });
            }
            return '';
        }

        const decoder = new TextDecoder();
        let resultString = '';
        let resultChunk = await streamReader.read();
        while (!resultChunk.done) {
            if (isProblematicPodcast) {
                this.logger.debug('ProblematicPodcast has read a chunk');
            }

            resultString += decoder.decode(resultChunk.value);

            if (isProblematicPodcast) {
                this.logger.debug('ProblematicPodcast has added to result string', {
                    length: resultString.length,
                });
            }

            if (this.firstPubDatesAreSequential(resultString)) {
                if (isProblematicPodcast) {
                    this.logger.debug('ProblematicPodcast is done checking pubdates', {
                        length: resultString.length,
                    });
                }

                clearTimeout(timeoutId);

                // This feels a little sketchy to abort the controller and return but it works.
                controller.abort();
                return this.cleanPartialPodcastFeed(resultString);
            }

            if (isProblematicPodcast) {
                this.logger.debug('ProblematicPodcast starting reading another chunk', {
                    length: resultString.length,
                });
            }

            resultChunk = await streamReader.read();

            if (isProblematicPodcast) {
                this.logger.debug('ProblematicPodcast completed reading another chunk', {
                    length: resultString.length,
                });
            }
        }

        if (isProblematicPodcast) {
            this.logger.debug('ProblematicPodcast done with the while loop', {
                length: resultString.length,
            });
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
            new Date(pubDates[0][1]).getTime() >= new Date(pubDates[1][1]).getTime();
        const secondPubDateisClosed = input.indexOf('</item>', pubDates[1]['index']) > 0;

        return isSequential && secondPubDateisClosed;
    }

    private cleanPartialPodcastFeed(input: string) {
        const lastItemClose = input.lastIndexOf('</item>');
        const unclosedItemString = input.substring(0, lastItemClose);

        return unclosedItemString + '</item></channel></rss>';
    }
}

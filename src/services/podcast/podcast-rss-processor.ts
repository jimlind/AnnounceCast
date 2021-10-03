import { RESOLVER } from 'awilix';
import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { Node } from 'domhandler';
import * as htmlparser2 from 'htmlparser2';
import { PodcastEpisode } from '../../models/podcast-episode.js';
import { Podcast } from '../../models/podcast.js';

export class PodcastRssProcessor {
    static [RESOLVER] = {};

    axios: AxiosInstance;
    htmlParser2: typeof htmlparser2;

    constructor(axios: AxiosInstance, htmlParser2: typeof htmlparser2) {
        this.axios = axios;
        this.htmlParser2 = htmlParser2;
    }

    process(feedUrl: string, episodeCount: number): Promise<Podcast> {
        const cancelTokenSource = axios.CancelToken.source();
        const timeoutId = setTimeout(() => cancelTokenSource.cancel(), 60000); // 1 minute

        return this.axios
            .get(feedUrl, { cancelToken: cancelTokenSource.token })
            .then((response) => {
                clearTimeout(timeoutId); // Clear the 1 minutes timeout
                const parsedData = this._parseRSS(feedUrl, episodeCount, response.data);
                if (!(parsedData instanceof Podcast)) {
                    throw 'Unable to parse feed.';
                }
                return parsedData;
            });
    }

    _parseRSS(feedUrl: string, episodeCount: number, responseText: string): Podcast | null {
        const domUtils = this.htmlParser2.DomUtils;
        const document = this.htmlParser2.parseDocument(responseText, { xmlMode: true });

        const rssElement = domUtils.getElementsByTagName('rss', document);
        if (!rssElement.length) {
            return null;
        }

        const podcast = new Podcast();
        podcast.title = this._getTextByTag(document, ['title']);
        podcast.description = this._getTextByTag(document, ['description']);
        podcast.author = this._getTextByTag(document, ['itunes:author']);
        podcast.image = this._getTextByTag(document, ['image', 'url']);
        podcast.link = this._getTextByTag(document, ['link']);
        podcast.feed = feedUrl;

        const episodeList = domUtils.getElementsByTagName('item', document, true, episodeCount);
        podcast.episodeList = episodeList.map((item) => {
            const podcastEpisode = new PodcastEpisode();

            podcastEpisode.guid = this._getTextByTag(item, ['guid']);
            podcastEpisode.title = this._getTextByTag(item, ['title']);
            podcastEpisode.number = this._getTextByTag(item, ['itunes:episode']);
            podcastEpisode.season = this._getTextByTag(item, ['itunes:season']);
            podcastEpisode.link = this._getTextByTag(item, ['link']);
            podcastEpisode.duration = this._parseDurationText(
                this._getTextByTag(item, ['itunes:duration']),
            );
            podcastEpisode.explicit =
                this._getTextByTag(item, ['itunes:explicit']).toUpperCase() == 'YES';

            const enclosure = domUtils.getElementsByTagName('enclosure', item, true, 1);
            podcastEpisode.audio = domUtils.getAttributeValue(enclosure[0] || [], 'url') || '';

            const image = domUtils.getElementsByTagName('itunes:image', item, true, 1);
            podcastEpisode.image = domUtils.getAttributeValue(image[0] || [], 'href') || '';

            const description = domUtils.getElementsByTagName('description', item, true, 1);
            const descriptionDocument = this.htmlParser2.parseDocument(
                domUtils.getText(description),
            );
            // Get the text from the first paragraph tag
            const paragraphs = domUtils.getElementsByTagName('p', descriptionDocument, true, 1);
            podcastEpisode.description = domUtils.getText(paragraphs);

            return podcastEpisode;
        });

        return podcast;
    }

    _parseDurationText(duration: string): number {
        if (!duration.includes(':')) {
            return parseInt(duration);
        }

        const pieces = duration.split(':').map((x) => parseInt(x));
        const piecesReversed = pieces.reverse();
        return piecesReversed.reduce((accumulator, current, index) => {
            return accumulator + current * Math.pow(60, index);
        }, 0);
    }

    _getTextByTag(node: Node | Node[], tagList: string[]): string {
        const domUtils = this.htmlParser2.DomUtils;
        const element = domUtils.getElementsByTagName(tagList.shift() || '', node, true, 1);

        if (tagList.length == 0) {
            return domUtils.getText(element);
        } else {
            return this._getTextByTag(element, tagList);
        }
    }
}

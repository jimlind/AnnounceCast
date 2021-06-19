import { RESOLVER } from 'awilix';
import { AxiosInstance, AxiosResponse } from 'axios';
import { boolean } from 'boolean';
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
        return new Promise((resolve, reject) => {
            this.axios
                .get(feedUrl)
                .then((response: AxiosResponse) => {
                    return resolve(this._parseRSS(feedUrl, episodeCount, response.data));
                })
                .catch(() => {
                    return reject('Failed to download or parse feed');
                });
        });
    }

    _parseRSS(feedUrl: string, episodeCount: number, responseText: string): Podcast {
        const domUtils = this.htmlParser2.DomUtils;
        const document = this.htmlParser2.parseDocument(responseText, { xmlMode: true });

        const podcast = new Podcast();
        podcast.title = this._getTextByTag(document, ['title']);
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
            podcastEpisode.explicit = boolean(this._getTextByTag(item, ['itunes:explicit']));

            const enclosure = domUtils.getElementsByTagName('enclosure', item, true, 1);
            podcastEpisode.audio = domUtils.getAttributeValue(enclosure[0], 'url') || '';

            const image = domUtils.getElementsByTagName('itunes:image', item, true, 1);
            podcastEpisode.image = domUtils.getAttributeValue(image[0], 'href') || '';

            const description = domUtils.getElementsByTagName('description', item, true, 1);
            const descriptionDocument = this.htmlParser2.parseDocument(
                domUtils.getText(description),
            );
            const paragraphs = domUtils.getElementsByTagName('p', descriptionDocument, true, 1);
            podcastEpisode.description = domUtils.getText(paragraphs);

            return podcastEpisode;
        });

        return podcast;
    }

    _parseDurationText(duration: string): string {
        if (!duration.includes(':')) {
            return duration;
        }

        const partialDuration = duration.split(':');
        return (
            parseInt(partialDuration[0]) * 60 * 60 +
            parseInt(partialDuration[1]) * 60 +
            parseInt(partialDuration[2])
        ).toString();
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

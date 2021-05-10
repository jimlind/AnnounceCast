import { RESOLVER } from 'awilix';
import { AxiosInstance, AxiosResponse } from 'axios';
import { boolean } from 'boolean';
import * as htmlparser2 from 'htmlparser2';
import { Podcast } from '../../models/podcast';

export class PodcastProcessor {
    static [RESOLVER] = {};

    axios: AxiosInstance;
    htmlParser2: typeof htmlparser2;

    constructor(axios: AxiosInstance, htmlParser2: typeof htmlparser2) {
        this.axios = axios;
        this.htmlParser2 = htmlParser2;
    }

    process(feedUrl: string): Promise<Podcast> {
        return new Promise((resolve, reject) => {
            this.axios
                .get(feedUrl)
                .then((response: AxiosResponse) => {
                    return resolve(this.parseRSS(response.data));
                })
                .catch(() => {
                    return reject('Failed to download or parse feed');
                });
        });
    }

    parseRSS(responseText: any): Podcast {
        const domUtils = this.htmlParser2.DomUtils;
        const document = this.htmlParser2.parseDocument(responseText, { xmlMode: true });

        const podcast = new Podcast();

        const showTitle = domUtils.getElementsByTagName('title', document, true, 1);
        podcast.showTitle = domUtils.getText(showTitle);

        const showLink = domUtils.getElementsByTagName('link', document, true, 1);
        podcast.showLink = domUtils.getText(showLink);

        const showImage = domUtils.getElementsByTagName('image', document, true, 1);
        const showImageUrl = domUtils.getElementsByTagName('url', showImage, true, 1);
        podcast.showImage = domUtils.getText(showImageUrl);

        const episode = domUtils.getElementsByTagName('item', document, true, 1);

        const seasonNumber = domUtils.getElementsByTagName('itunes:season', episode, true, 1);
        podcast.seasonNumber = domUtils.getText(seasonNumber);

        const episodeNumber = domUtils.getElementsByTagName('itunes:episode', episode, true, 1);
        podcast.episodeNumber = domUtils.getText(episodeNumber);

        const episodeGuid = domUtils.getElementsByTagName('guid', episode, true, 1);
        podcast.episodeGuid = domUtils.getText(episodeGuid);

        const episodeLink = domUtils.getElementsByTagName('link', episode, true, 1);
        podcast.episodeLink = domUtils.getText(episodeLink);

        const episodeExplicit = domUtils.getElementsByTagName('itunes:explicit', episode, true, 1);
        podcast.episodeExplicit = boolean(domUtils.getText(episodeExplicit));

        var episodeDuration = domUtils.getElementsByTagName('itunes:duration', episode, true, 1);
        var episodeDurationText = domUtils.getText(episodeDuration);
        if (episodeDurationText.includes(':')) {
            const t = episodeDurationText.split(':');
            episodeDurationText = (
                parseInt(t[0]) * 60 * 60 +
                parseInt(t[1]) * 60 +
                parseInt(t[2])
            ).toString();
        }
        podcast.episodeDuration = episodeDurationText;

        const episodeImage = domUtils.getElementsByTagName('itunes:image', episode, true, 1);
        if (episodeImage.length === 1) {
            podcast.episodeImage = domUtils.getAttributeValue(episodeImage[0], 'href') || '';
        }

        const episodeTitle = domUtils.getElementsByTagName('title', episode, true, 1);
        podcast.episodeTitle = domUtils.getText(episodeTitle);

        const description = domUtils.getElementsByTagName('description', episode, true, 1);
        const descriptionDocument = this.htmlParser2.parseDocument(domUtils.getText(description));
        const paragraphs = domUtils.getElementsByTagName('p', descriptionDocument, true, 1);
        podcast.episodeDescription = domUtils.getText(paragraphs);

        return podcast;
    }
}

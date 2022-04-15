import normalizeUrl from 'normalize-url';
import { PodcastEpisode } from './podcast-episode.js';

export class Podcast {
    private _title: string = '';
    public get title(): string {
        return this._title;
    }
    public set title(value: string) {
        this._title = value;
    }

    private _description: string = '';
    public get description(): string {
        return this._description;
    }
    public set description(value: string) {
        this._description = value;
    }

    private _author: string = '';
    public get author(): string {
        return this._author;
    }
    public set author(value: string) {
        this._author = value;
    }

    private _image: string = '';
    public get image(): string {
        return this._image;
    }
    public set image(value: string) {
        this._image = value;
    }

    private _link: string = '';
    public get link(): string {
        try {
            return normalizeUrl(this._link);
        } catch {
            return '';
        }
    }
    public set link(value: string) {
        this._link = value;
    }

    private _feed: string = '';
    public get feed(): string {
        return this._feed;
    }
    public set feed(value: string) {
        this._feed = value;
    }

    private _episodeList: PodcastEpisode[] = [];
    public get episodeList(): PodcastEpisode[] {
        return this._episodeList;
    }
    public set episodeList(value: PodcastEpisode[]) {
        this._episodeList = value;
    }

    // Get the first episode or return an empty episode object
    public getFirstEpisode(): PodcastEpisode {
        return this._episodeList[0] || new PodcastEpisode();
    }
}

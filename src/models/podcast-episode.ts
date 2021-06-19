import prettyMilliseconds from 'pretty-ms';

export class PodcastEpisode {
    private _audio: string = '';
    public get audio(): string {
        return this._audio;
    }
    public set audio(value: string) {
        this._audio = value;
    }

    private _title: string = '';
    public get title(): string {
        return this._title;
    }
    public set title(value: string) {
        this._title = value;
    }

    private _number: string = '';
    public get number(): string {
        return this._number;
    }
    public set number(value: string) {
        this._number = value;
    }

    private _season: string = '';
    public get season(): string {
        return this._season;
    }
    public set season(value: string) {
        this._season = value;
    }

    private _guid: string = '';
    public get guid(): string {
        return this._guid;
    }
    public set guid(value: string) {
        this._guid = value;
    }

    private _link: string = '';
    public get link(): string {
        return this._link;
    }
    public set link(value: string) {
        this._link = value;
    }

    private _image: string = '';
    public get image(): string {
        return this._image;
    }
    public set image(value: string) {
        this._image = value;
    }

    private _description: string = '';
    public get description(): string {
        return this._description;
    }
    public set description(value: string) {
        this._description = value;
    }

    private _duration: string = '';
    public get duration(): string {
        return prettyMilliseconds(parseInt(this._duration) * 1000);
    }
    public set duration(value: string) {
        this._duration = value;
    }

    private _explicit: boolean = false;
    public get explicit(): boolean {
        return this._explicit;
    }
    public set explicit(value: boolean) {
        this._explicit = value;
    }
}

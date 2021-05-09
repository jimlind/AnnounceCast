export class Podcast {
    private _showTitle: string = '';
    public get showTitle(): string {
        return this._showTitle;
    }
    public set showTitle(value: string) {
        this._showTitle = value;
    }

    private _showImage: string = '';
    public get showImage(): string {
        return this._showImage;
    }
    public set showImage(value: string) {
        this._showImage = value;
    }

    private _episodeGuid: string = '';
    public get episodeGuid(): string {
        return this._episodeGuid;
    }
    public set episodeGuid(value: string) {
        this._episodeGuid = value;
    }

    private _episodeLink: string = '';
    public get episodeLink(): string {
        return this._episodeLink;
    }
    public set episodeLink(value: string) {
        this._episodeLink = value;
    }

    private _episodeTitle: string = '';
    public get episodeTitle(): string {
        return this._episodeTitle;
    }
    public set episodeTitle(value: string) {
        this._episodeTitle = value;
    }

    private _episodeImage: string = '';
    public get episodeImage(): string {
        return this._episodeImage;
    }
    public set episodeImage(value: string) {
        this._episodeImage = value;
    }

    private _episodeDescription: string = '';
    public get episodeDescription(): string {
        return this._episodeDescription;
    }
    public set episodeDescription(value: string) {
        this._episodeDescription = value;
    }
}

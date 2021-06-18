export class PodcastFeedRow {
    private _id: string = '';
    public get id(): string {
        return this._id;
    }
    public set id(value: string) {
        this._id = value;
    }

    private _url: string = '';
    public get url(): string {
        return this._url;
    }
    public set url(value: string) {
        this._url = value;
    }

    private _title: string = '';
    public get title(): string {
        return this._title;
    }
    public set title(value: string) {
        this._title = value;
    }
}

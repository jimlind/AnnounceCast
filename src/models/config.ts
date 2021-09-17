export class Config {
    private _appName: string = '';
    public get appName(): string {
        return this._appName;
    }
    public set appName(value: string) {
        this._appName = value;
    }

    private _appVersion: string = '';
    public get appVersion(): string {
        return this._appVersion;
    }
    public set appVersion(value: string) {
        this._appVersion = value;
    }

    private _discordBotToken: string = '';
    public get discordBotToken(): string {
        return this._discordBotToken;
    }
    public set discordBotToken(value: string) {
        this._discordBotToken = value;
    }

    private _discordClientId: string = '';
    public get discordClientId(): string {
        return this._discordClientId;
    }
    public set discordClientId(value: string) {
        this._discordClientId = value;
    }
}

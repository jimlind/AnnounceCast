export class Config {
    private _discordBotToken: string = '';
    public get discordBotToken(): string {
        return this._discordBotToken;
    }
    public set discordBotToken(value: string) {
        this._discordBotToken = value;
    }
}

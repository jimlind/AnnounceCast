export class Message {
    // Was the message sent from a user with manage permissions
    private _manageServer: boolean = false;
    public get manageServer(): boolean {
        return this._manageServer;
    }
    public set manageServer(value: boolean) {
        this._manageServer = value;
    }

    // The first string in the message
    private _command: string = '';
    public get command(): string {
        return this._command;
    }
    public set command(value: string) {
        this._command = value;
    }

    // Subsequent strings in the message
    private _arguments: string[] = [''];
    public get arguments(): string[] {
        return this._arguments;
    }
    public set arguments(value: string[]) {
        this._arguments = value;
    }

    // Channel the message was sent from
    private _channelId: string = '0';
    public get channelId(): string {
        return this._channelId;
    }
    public set channelId(value: string) {
        this._channelId = value;
    }
}

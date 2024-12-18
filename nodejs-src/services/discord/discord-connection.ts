interface DiscordConnectionInterface {
    readonly config: typeof import('../../config.js').default;
    readonly discordClient: import('discord.js').Client;
    readonly discordEvents: typeof import('discord.js').Events;
    readonly logger: import('log4js').Logger;

    getClient(): Promise<import('discord.js').Client>;
}

export default class DiscordConnection implements DiscordConnectionInterface {
    private locked: boolean = false;

    constructor(
        readonly config: typeof import('../../config.js').default,
        readonly discordClient: import('discord.js').Client,
        readonly discordEvents: typeof import('discord.js').Events,
        readonly logger: import('log4js').Logger,
    ) {}

    async getClient(): Promise<import('discord.js').Client<true>> {
        // If the client is ready return it
        if (this.discordClient.isReady()) {
            return this.discordClient;
        }

        // If no token set reject the request
        if (!this.config.discord.botToken) {
            throw new Error('Error: No Discord Bot Token set');
        }

        // If the connecting process is already happening reject additional attempts
        // Multiple connections means something terrible has happened
        if (this.locked) {
            throw new Error('Error: Can not make duplicate connection attempts');
        }
        // Lock it up
        this.locked = true;

        // Event listener for when client is ready
        this.discordClient.once(this.discordEvents.ClientReady, (readyClient) => {
            this.logger.info(`Ready! Logged in as ${readyClient.user.tag}`);
            this.locked = false;
        });

        // Start the login process that will make the client ready
        await this.discordClient.login(this.config.discord.botToken);

        // Connecting process is active so recursively wait
        return new Promise((resolve) => {
            const delay = 200; // Completely arbitrary timing choice
            const getClient = () => {
                if (this.discordClient.isReady()) {
                    resolve(this.discordClient);
                } else {
                    setTimeout(getClient, delay);
                }
            };
            setTimeout(getClient, delay);
        });
    }
}

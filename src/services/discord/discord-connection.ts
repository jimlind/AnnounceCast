import { RESOLVER } from 'awilix';

export class DiscordConnection {
    static [RESOLVER] = {};

    config: typeof import('../../config.js').default;
    discordClient: import('discord.js').Client;
    discordEvents: typeof import('discord.js').Events;
    logger: import('log4js').Logger;

    connected: boolean = false;
    locked: boolean = false;

    constructor(
        config: typeof import('../../config.js').default,
        discordClient: import('discord.js').Client,
        discordEvents: typeof import('discord.js').Events,
        logger: import('log4js').Logger,
    ) {
        this.config = config;
        this.discordClient = discordClient;
        this.discordEvents = discordEvents;
        this.logger = logger;
    }

    async getClient(): Promise<import('discord.js').Client> {
        // If no token set reject the request
        if (!this.config.discord.botToken) {
            throw new Error('Error: No Discord Bot Token set');
        }

        // If the connecting process is already happening reject additional attempts
        // Multiple connections means something terrible has happened
        if (this.locked) {
            throw new Error('Error: Can not make duplicate connection attempts');
        }

        // If the client is connected return it
        if (this.connected) {
            return this.discordClient;
        }

        // Indicate the connecting process is active
        this.locked = true;

        this.discordClient.once(this.discordEvents.ClientReady, (readyClient) => {
            this.logger.info(`Ready! Logged in as ${readyClient.user.tag}`);
            this.connected = true;
            this.locked = false;
        });

        await this.discordClient.login(this.config.discord.botToken);
        return this.discordClient;
    }
}

import { RESOLVER } from 'awilix';

export class DiscordConnection {
    static [RESOLVER] = {};

    config: typeof import('../../config.js').default;
    discordClient: import('discord.js').Client;
    discordEvents: typeof import('discord.js').Events;

    connected: boolean = false;
    locked: boolean = false;

    constructor(
        config: typeof import('../../config.js').default,
        discordClient: import('discord.js').Client,
        discordEvents: typeof import('discord.js').Events,
    ) {
        this.config = config;
        this.discordClient = discordClient;
        this.discordEvents = discordEvents;
    }

    async getClient(): Promise<import('discord.js').Client> {
        // If no token set reject the request
        if (!this.config.discord.botToken) {
            throw new Error('No Discord Bot Token Set');
        }

        // If the connecting process is already happening reject additional attempts
        // Multiple connections means something terrible has happened
        if (this.locked) {
            throw new Error('Can Not Make Duplicate Connection Attempts');
        }

        // If the client is connected return it
        if (this.connected) {
            return this.discordClient;
        }

        // Indicate the connecting process is active
        this.locked = true;

        this.discordClient.once(this.discordEvents.ClientReady, (readyClient) => {
            console.log(`Ready! Logged in as ${readyClient.user.tag}`);
            this.connected = true;
            this.locked = false;
        });

        await this.discordClient.login(this.config.discord.botToken);
        return this.discordClient;
    }
}

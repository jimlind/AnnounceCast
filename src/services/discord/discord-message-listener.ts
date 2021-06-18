import { RESOLVER } from 'awilix';
import * as discordJS from 'discord.js';
import { DiscordConnection } from './discord-connection';
import { DiscordDataStorage } from './discord-data-storage.js';

export class DiscordMessageListener {
    static [RESOLVER] = {};

    discordConnection: DiscordConnection;
    discordDataStorage: DiscordDataStorage;

    constructor(discordConnection: DiscordConnection, discordDataStorage: DiscordDataStorage) {
        this.discordConnection = discordConnection;
        this.discordDataStorage = discordDataStorage;
    }

    onMessage(callback: Function) {
        // Get connected client and listen for messages
        this.discordConnection.getConnectedClient().then((client) => {
            client.on('message', (message: discordJS.Message) => {
                // Skip messges from bots
                if (message.author.bot) {
                    return;
                }

                // Only process actions that start with the correct prefix or global
                const prefix = this.discordDataStorage.getPrefix(message.guild?.id || '');
                [prefix, '?podcasts'].forEach((value) => {
                    if (message.content.startsWith(value)) {
                        callback(this.createUserTextMessage(value, message));
                    }
                });
            });
        });
    }
}

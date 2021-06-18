import { RESOLVER } from 'awilix';
import { Message } from 'discord.js';
import { IncomingMessageFactory } from '../incoming-message/incoming-message-factory';
import { DiscordConnection } from './discord-connection';
import { DiscordDataStorage } from './discord-data-storage.js';

export class DiscordMessageListener {
    static [RESOLVER] = {}; // So Awilix autoloads the class
    MESSAGE_ACTION_KEY = 'message';

    discordConnection: DiscordConnection;
    discordDataStorage: DiscordDataStorage;
    incomingMessageFactory: IncomingMessageFactory;
    magicPrefix: string;

    constructor(
        discordConnection: DiscordConnection,
        discordDataStorage: DiscordDataStorage,
        incomingMessageFactory: IncomingMessageFactory,
        magicPrefix: string,
    ) {
        this.discordConnection = discordConnection;
        this.discordDataStorage = discordDataStorage;
        this.incomingMessageFactory = incomingMessageFactory;
        this.magicPrefix = magicPrefix;
    }

    onMessage(callback: Function) {
        // Get connected client and listen for messages
        this.discordConnection.getConnectedClient().then((client) => {
            client.on(this.MESSAGE_ACTION_KEY, (message: Message) => {
                // Skip messges from bots
                if (message.author.bot) {
                    return;
                }

                // Only process actions that start with the correct prefix or global
                const guildId = message.guild?.id || '';
                const prefixList = [this.discordDataStorage.getPrefix(guildId), this.magicPrefix];
                prefixList.forEach((value) => {
                    if (message.content.startsWith(value)) {
                        callback(this.incomingMessageFactory.build(value, message));
                    }
                });
            });
        });
    }
}

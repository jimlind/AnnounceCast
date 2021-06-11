import { RESOLVER } from 'awilix';
import * as discordJS from 'discord.js';
import { Message } from '../../models/message.js';
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

    createUserTextMessage(prefix: string, discordMessage: discordJS.Message) {
        // Convert to plain lower case text split on the first space
        const messageTextList = discordMessage.content.split(/ +/);

        // Callback on the message parsed into a subscription message object
        const userMessage = new Message();

        userMessage.manageServer = discordMessage.member?.permissions.has('MANAGE_GUILD') || false;
        userMessage.command = messageTextList[0].substring(prefix.length);
        userMessage.guildId = discordMessage.guild?.id || '';
        userMessage.channelId = discordMessage.channel.id;
        userMessage.arguments = messageTextList.slice(1);

        const voiceChannel = discordMessage.member?.voice.channel || null;
        const user = discordMessage.client.user || '';
        if (voiceChannel && voiceChannel.permissionsFor(user)?.has('SPEAK')) {
            userMessage.voiceChannel = voiceChannel;
        }

        return userMessage;
    }
}

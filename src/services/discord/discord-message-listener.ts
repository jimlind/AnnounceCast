import { RESOLVER } from 'awilix';
import { Message } from '../../models/message.js';
import { DiscordConnection } from './discord-connection';
import * as discordJS from 'discord.js';

export class DiscordMessageListener {
    static [RESOLVER] = {};

    discordConnection: DiscordConnection;

    constructor(discordConnection: DiscordConnection) {
        this.discordConnection = discordConnection;
    }

    onMessage(callback: Function) {
        // Get connected client and listen for messages
        this.discordConnection.getConnectedClient().then((client) => {
            client.on('message', (message: discordJS.Message) => {
                // Only process actions that start with a '!`
                if (message.content[0] === '!') {
                    callback(this.createUserTextMessage(message));
                }
            });
        });
    }

    createUserTextMessage(discordMessage: discordJS.Message) {
        // Convert to plain lower case text split on the first space
        const messageTextList = discordMessage.content.toLowerCase().split(/ +/);

        // Callback on the message parsed into a subscription message object
        const userMessage = new Message();

        userMessage.manageServer = discordMessage.member?.permissions.has('MANAGE_GUILD') || false;
        userMessage.command = messageTextList[0].substring(1);
        userMessage.channelId = discordMessage.channel.id;
        userMessage.arguments = messageTextList.slice(1);

        return userMessage;
    }
}

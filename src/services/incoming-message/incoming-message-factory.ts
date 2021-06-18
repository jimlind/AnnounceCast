import * as discordJS from 'discord.js';
import { IncomingMessage } from '../../models/incoming-message.js';

export class IncomingMessageFactory {
    build(prefix: string, discordMessage: discordJS.Message) {
        // Convert to plain lower case text split on the first space
        const messageTextList = discordMessage.content.split(/ +/);

        // Callback on the message parsed into a subscription message object
        const incomingMessage = new IncomingMessage();

        // Parse command and arguments
        incomingMessage.command = messageTextList[0].substring(prefix.length);
        incomingMessage.arguments = messageTextList.slice(1);

        // Set some additional data
        incomingMessage.guildId = discordMessage.guild?.id || '';
        incomingMessage.channelId = discordMessage.channel.id;

        // If the message author has manage server permissions
        incomingMessage.fromServerManager =
            discordMessage.member?.permissions.has('MANAGE_GUILD') || false;

        // A voice channel the message author is also in
        incomingMessage.voiceChannel = discordMessage.member?.voice.channel || null;

        return incomingMessage;
    }
}

import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Podcast } from '../../../models/podcast';
import { OutgoingMessageHelpers } from '../outgoing-message-helpers';

export class PodcastInfo {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    outgoingMessageHelpers: OutgoingMessageHelpers;
    constructor(outgoingMessageHelpers: OutgoingMessageHelpers) {
        this.outgoingMessageHelpers = outgoingMessageHelpers;
    }

    build(message: MessageEmbed, podcast: Podcast): MessageEmbed {
        message.setTitle(podcast.title);
        message.setThumbnail(podcast.image);
        message.setDescription(this._getDescription(podcast));
        message.setFooter({ text: `Credit: ${podcast.author}` });

        return message;
    }

    _getDescription(podcast: Podcast): string {
        const compressedDescription = this.outgoingMessageHelpers.compressPodcastDescription(
            podcast.description,
        );

        return (
            compressedDescription +
            '\n\n' +
            `Show Feed URL: ${podcast.feed}` +
            '\n' +
            `Show's Website: ${podcast.link}`
        );
    }
}

import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { PodcastFeedRow } from '../../../models/db/podcast-feed-row';
import { Podcast } from '../../../models/podcast';
import { OutgoingMessageHelpers } from '../outgoing-message-helpers';

export class Followed {
    static [RESOLVER] = {}; // So Awilix autoloads the class
    outgoingMessageHelpers: OutgoingMessageHelpers;

    constructor(outgoingMessageHelpers: OutgoingMessageHelpers) {
        this.outgoingMessageHelpers = outgoingMessageHelpers;
    }

    build(message: MessageEmbed, podcast: Podcast, rows: PodcastFeedRow[]): MessageEmbed {
        message.setTitle('You are now following ' + podcast.title);
        message.setThumbnail(podcast.image);

        const compressedDescription = this.outgoingMessageHelpers.compressPodcastDescription(
            podcast.description,
        );
        const gridString = this.outgoingMessageHelpers.feedRowsToGridString(rows);
        message.setDescription(compressedDescription + '\n\n```\n' + gridString + '\n```');

        return message;
    }
}

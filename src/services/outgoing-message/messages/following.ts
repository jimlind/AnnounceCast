import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { PodcastFeedRow } from '../../../models/db/podcast-feed-row';
import { OutgoingMessageHelpers } from '../outgoing-message-helpers';

export class Following {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    outgoingMessageHelpers: OutgoingMessageHelpers;
    constructor(outgoingMessageHelpers: OutgoingMessageHelpers) {
        this.outgoingMessageHelpers = outgoingMessageHelpers;
    }

    build(message: MessageEmbed, rows: PodcastFeedRow[]): MessageEmbed {
        message.setTitle('Podcasts Followed in this Channel');

        const gridString = this.outgoingMessageHelpers.feedRowsToGridString(rows);
        message.setDescription('```\n' + gridString + '\n```');

        return message;
    }
}

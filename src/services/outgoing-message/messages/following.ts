import { EmbedBuilder } from 'discord.js';
import PodcastFeedRow from '../../../models/db/podcast-feed-row';
import OutgoingMessageHelpers from '../outgoing-message-helpers';

interface FollowingInterface {
    build(message: EmbedBuilder, rows: PodcastFeedRow[]): EmbedBuilder;
}

export default class Following implements FollowingInterface {
    constructor(readonly outgoingMessageHelpers: OutgoingMessageHelpers) {}

    build(message: EmbedBuilder, rows: PodcastFeedRow[]): EmbedBuilder {
        message.setTitle('Podcasts Followed in this Channel');

        const gridString = this.outgoingMessageHelpers.feedRowsToGridString(rows);
        message.setDescription('```\n' + gridString + '\n```');

        return message;
    }
}

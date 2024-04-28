import { EmbedBuilder } from 'discord.js';
import PodcastFeedRow from '../../../models/db/podcast-feed-row.js';
import OutgoingMessageHelpers from '../outgoing-message-helpers.js';

interface FollowingInterface {
    readonly outgoingMessageHelpers: OutgoingMessageHelpers;

    build(message: EmbedBuilder, rows: PodcastFeedRow[]): EmbedBuilder;
}

export default class Following implements FollowingInterface {
    constructor(readonly outgoingMessageHelpers: OutgoingMessageHelpers) {}

    public build(message: EmbedBuilder, rows: PodcastFeedRow[]): EmbedBuilder {
        message.setTitle('Podcasts Followed in this Channel');

        const gridString = this.outgoingMessageHelpers.feedRowsToGridString(rows);
        // TODO: This could error out if the gridString is too big
        // For now we can asssume that most bot users won't have enough podcasts followed that it will cause any problems
        message.setDescription('```\n' + gridString + '\n```');

        return message;
    }
}

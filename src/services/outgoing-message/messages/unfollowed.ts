import { EmbedBuilder } from 'discord.js';
import PodcastFeedRow from '../../../models/db/podcast-feed-row.js';
import OutgoingMessageHelpers from '../outgoing-message-helpers.js';

interface UnollowedInterface {
    readonly outgoingMessageHelpers: OutgoingMessageHelpers;

    build(embedBuilder: EmbedBuilder, title: string, rows: PodcastFeedRow[]): EmbedBuilder;
}

export default class Unfollowed implements UnollowedInterface {
    constructor(readonly outgoingMessageHelpers: OutgoingMessageHelpers) {}

    public build(embedBuilder: EmbedBuilder, title: string, rows: PodcastFeedRow[]): EmbedBuilder {
        embedBuilder.setTitle(`You are no longer following ${title}`);

        const gridString = this.outgoingMessageHelpers.feedRowsToGridString(rows, true);
        embedBuilder.setDescription('```\n' + gridString + '\n```');

        return embedBuilder;
    }
}

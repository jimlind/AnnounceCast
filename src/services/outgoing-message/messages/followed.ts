import { EmbedBuilder } from 'discord.js';
import { Podcast } from 'podparse';
import PodcastFeedRow from '../../../models/db/podcast-feed-row.js';
import OutgoingMessageHelpers from '../outgoing-message-helpers.js';

interface FollowedInterface {
    readonly outgoingMessageHelpers: OutgoingMessageHelpers;

    build(embedBuilder: EmbedBuilder, podcast: Podcast, rows: PodcastFeedRow[]): EmbedBuilder;
}

export default class Followed implements FollowedInterface {
    constructor(readonly outgoingMessageHelpers: OutgoingMessageHelpers) {}

    public build(
        embedBuilder: EmbedBuilder,
        podcast: Podcast,
        rows: PodcastFeedRow[],
    ): EmbedBuilder {
        embedBuilder.setTitle('You are now following ' + podcast.meta.title);
        embedBuilder.setThumbnail(podcast.meta.image.url);

        const description = podcast.meta.description
            ? podcast.meta.description
            : podcast.meta.summary;
        const compressedDescription =
            this.outgoingMessageHelpers.compressPodcastDescription(description);
        const gridString = this.outgoingMessageHelpers.feedRowsToGridString(rows);
        embedBuilder.setDescription(compressedDescription + '\n\n```\n' + gridString + '\n```');

        return embedBuilder;
    }
}

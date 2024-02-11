import { EmbedBuilder } from 'discord.js';
import { Podcast } from 'podparse';
import PodcastFeedRow from '../../../models/db/podcast-feed-row';
import OutgoingMessageHelpers from '../outgoing-message-helpers';

interface FollowedInterface {
    readonly outgoingMessageHelpers: OutgoingMessageHelpers;

    build(embedBuilder: EmbedBuilder, podcast: Podcast, rows: PodcastFeedRow[]): EmbedBuilder;
}

export default class Followed {
    constructor(readonly outgoingMessageHelpers: OutgoingMessageHelpers) {}

    build(embedBuilder: EmbedBuilder, podcast: Podcast, rows: PodcastFeedRow[]): EmbedBuilder {
        embedBuilder.setTitle('You are now following ' + podcast.meta.title);
        embedBuilder.setThumbnail(podcast.meta.image.url);

        const compressedDescription = this.outgoingMessageHelpers.compressPodcastDescription(
            podcast.meta.description,
        );
        const gridString = this.outgoingMessageHelpers.feedRowsToGridString(rows);
        embedBuilder.setDescription(compressedDescription + '\n\n```\n' + gridString + '\n```');

        return embedBuilder;
    }
}

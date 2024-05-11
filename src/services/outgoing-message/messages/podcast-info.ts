import { EmbedBuilder } from 'discord.js';
import { Podcast } from 'podparse';
import OutgoingMessageHelpers from '../outgoing-message-helpers.js';

interface PodcastInfoInterface {
    build(message: EmbedBuilder, podcast: Podcast): EmbedBuilder;
}

export default class PodcastInfo implements PodcastInfoInterface {
    constructor(readonly outgoingMessageHelpers: OutgoingMessageHelpers) {}

    public build(message: EmbedBuilder, podcast: Podcast): EmbedBuilder {
        message.setTitle(podcast.meta.title);
        message.setThumbnail(podcast.meta.image.url);
        message.setDescription(this.getDescription(podcast));
        message.setFooter({ text: `Credit: ${podcast.meta.author}` });

        return message;
    }

    private getDescription(podcast: Podcast): string {
        const formattedDescription = this.outgoingMessageHelpers.formatPodcastDescription(
            podcast.meta.description,
        );

        return (
            formattedDescription +
            '\n\n' +
            `Show Feed URL: ${podcast.meta.importFeedUrl}` +
            '\n' +
            `Show's Website: ${podcast.meta.link}`
        );
    }
}

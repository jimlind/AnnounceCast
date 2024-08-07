import {
    BaseGuildTextChannel,
    CacheType,
    ChatInputCommandInteraction,
    EmbedBuilder,
    PermissionsBitField,
    ThreadChannel,
} from 'discord.js';
import { Logger } from 'log4js';
import { Podcast } from 'podparse';
import OutgoingMessageFactory from '../outgoing-message/outgoing-message-factory.js';
import PodcastDataStorage from '../podcast/podcast-data-storage.js';
import PodcastHelpers from '../podcast/podcast-helpers.js';
import DiscordConnection from './discord-connection.js';

interface DiscordMessageSenderInterface {
    readonly discordConnection: DiscordConnection;
    readonly logger: Logger;
    readonly outgoingMessageFactory: OutgoingMessageFactory;
    readonly podcastDataStorage: PodcastDataStorage;
    readonly podcastHelpers: PodcastHelpers;

    sendMostRecentPodcastEpisode(podcast: Podcast, channelId: string | undefined): Promise<void>;
    sendMessageToChannel(channelId: string, embedBuilder: EmbedBuilder): Promise<void>;
    send(channelId: string, embedBuilder: EmbedBuilder): Promise<boolean>;
}

export default class DiscordMessageSender implements DiscordMessageSenderInterface {
    constructor(
        readonly discordConnection: DiscordConnection,
        readonly logger: Logger,
        readonly outgoingMessageFactory: OutgoingMessageFactory,
        readonly podcastDataStorage: PodcastDataStorage,
        readonly podcastHelpers: PodcastHelpers,
    ) {}

    public async sendMostRecentPodcastEpisode(
        podcast: Podcast,
        channelInput: string = '',
    ): Promise<void> {
        try {
            const outgoingMessage = this.outgoingMessageFactory.buildPodcastEpisodeMessage(podcast);
            const feedUrl = podcast.meta.importFeedUrl || '';

            let channelList = [channelInput];
            if (!channelInput) {
                channelList = this.podcastDataStorage.getChannelsByFeedUrl(feedUrl);
            }

            const mostRecentEpisode = this.podcastHelpers.getMostRecentPodcastEpisode(podcast);
            for (const channelId of channelList) {
                await this.sendMessageToChannel(channelId, outgoingMessage);
                this.podcastDataStorage.updatePostedData(feedUrl, mostRecentEpisode);
            }
        } catch (error) {
            const title = `Method 'sendMostRecentPodcastEpisode' Failed for Podcast: ${podcast?.meta.title}`;
            this.logger.info(title, { podcast, error });
        }
    }

    public async sendMessageToChannel(
        channelId: string,
        embedBuilder: EmbedBuilder,
    ): Promise<void> {
        try {
            await this.send(channelId, embedBuilder);
            this.logger.info(`Message Send Success  [${channelId}} | ${embedBuilder.data.title}]`);
        } catch (error) {
            this.logger.error(`Message Send Failure  [${channelId}} |${embedBuilder.data.title}]`, {
                message: embedBuilder.toJSON(),
                error,
            });
        }
        return;
    }

    public sendNoMatchesMessageAsReply(interaction: ChatInputCommandInteraction<CacheType>) {
        return interaction.editReply('Nothing was found matching your query.');
    }

    public sendErrorAsReply(interaction: ChatInputCommandInteraction<CacheType>) {
        const message =
            "Something went wrong. Check the bot's permissions, your input, and the podcast data.";
        return interaction.editReply(message);
    }

    public async send(channelId: string, embedBuilder: EmbedBuilder): Promise<boolean> {
        const discordClient = await this.discordConnection.getClient();
        const channel = discordClient.channels.cache.find((ch) => ch.id === channelId);

        // This covers all the current text channels (announcments, threads, etc)
        if (!(channel instanceof BaseGuildTextChannel) && !(channel instanceof ThreadChannel)) {
            return false;
        }

        const botPermissions = channel.permissionsFor(discordClient.user || '');
        // Check that the bot has some ability to send messages
        if (
            !botPermissions?.has(PermissionsBitField.Flags.SendMessages) &&
            !botPermissions?.has(PermissionsBitField.Flags.SendMessagesInThreads)
        ) {
            return false;
        }

        // Check that the bot has can view the channel and embed links
        if (
            !botPermissions?.has([
                PermissionsBitField.Flags.ViewChannel,
                PermissionsBitField.Flags.EmbedLinks,
            ])
        ) {
            return false;
        }

        // Try to send, but if there's a problem ignore it because it probably isn't anything important
        try {
            await channel.send({ embeds: [embedBuilder] });
        } catch (error) {
            return false;
        }

        return true;
    }
}

import { CacheType, ChatInputCommandInteraction, EmbedBuilder, Events } from 'discord.js';
import { Logger } from 'log4js';
import { Podcast } from 'podparse';
import DiscordConnection from './discord/discord-connection.js';
import DiscordMessageSender from './discord/discord-message-sender.js';
import HttpClient from './http-client.js';
import OutgoingMessageFactory from './outgoing-message/outgoing-message-factory.js';
import PodcastAppleAPIProcessor from './podcast/podcast-apple-api-processor.js';
import PodcastDataStorage from './podcast/podcast-data-storage.js';
import PodcastHelpers from './podcast/podcast-helpers.js';

interface BotInterface {
    readonly discordConnection: DiscordConnection;
    readonly discordEvents: Events;
    readonly discordMessageSender: DiscordMessageSender;
    readonly httpClient: HttpClient;
    readonly logger: Logger;
    readonly outgoingMessageFactory: OutgoingMessageFactory;
    readonly podcastAppleApiProcessor: PodcastAppleAPIProcessor;
    readonly podcastDataStorage: PodcastDataStorage;
    readonly podcastHelpers: PodcastHelpers;

    sendMostRecentPodcastEpisode(podcast: Podcast): Promise<void>;
    receiveInteraction(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class Bot implements BotInterface {
    constructor(
        readonly discordConnection: DiscordConnection,
        readonly discordEvents: Events,
        readonly discordMessageSender: DiscordMessageSender,
        readonly httpClient: HttpClient,
        readonly logger: Logger,
        readonly outgoingMessageFactory: OutgoingMessageFactory,
        readonly podcastAppleApiProcessor: PodcastAppleAPIProcessor,
        readonly podcastDataStorage: PodcastDataStorage,
        readonly podcastHelpers: PodcastHelpers,
    ) {}

    async receiveInteraction(interaction: ChatInputCommandInteraction<CacheType>) {
        try {
            switch (interaction.commandName) {
                case 'search':
                    await this.search(interaction);
                    break;
                case 'follow':
                    await this.follow(interaction);
                    break;
                case 'follow-rss':
                    await this.followRss(interaction);
                    break;
                case 'unfollow':
                    await this.unfollow(interaction);
                    break;
                case 'following':
                    await this.following(interaction);
                    break;
                default:
                    this.help(interaction);
                    break;
            }
        } catch (error) {
            const title = 'receiveInteraction method failed';
            this.logger.info(title, { command: interaction.commandName, interaction, error });
            this.sendErrorToChannel(interaction);
        }
    }

    async sendMostRecentPodcastEpisode(podcast: Podcast): Promise<void> {
        try {
            const outgoingMessage = this.outgoingMessageFactory.buildPodcastEpisodeMessage(podcast);

            const feedUrl = podcast.meta.importFeedUrl || '';
            const channelList = this.podcastDataStorage.getChannelsByFeedUrl(feedUrl);
            const mostRecentEpisode = this.podcastHelpers.getMostRecentPodcastEpisode(podcast);
            for (const channelId of channelList) {
                await this.sendMessageToChannel(channelId, outgoingMessage);
                this.podcastDataStorage.updatePostedData(feedUrl, mostRecentEpisode);
            }
        } catch (error) {
            const title = 'sendMostRecentPodcastEpisode method failed';
            this.logger.info(title, { podcast, error });
        }
    }

    private async search(interaction: ChatInputCommandInteraction<CacheType>) {
        const searchKeywords = interaction.options.getString('keywords') || '';
        const podcastList = await this.podcastAppleApiProcessor.search(searchKeywords, 4);
        if (podcastList.length == 0) {
            this.sendNoMatchesMessageToChannel(interaction);
            return;
        }

        const embedList = [];
        for (const podcast of podcastList) {
            const embed = await this.outgoingMessageFactory.buildPodcastInfoMessage(podcast);
            embedList.push(embed);
        }

        interaction.editReply({ embeds: embedList });
    }

    private async follow(interaction: ChatInputCommandInteraction<CacheType>) {
        const searchKeywords = interaction.options.getString('keywords') || '';
        const podcastList = await this.podcastAppleApiProcessor.search(searchKeywords, 1);

        this.followPodcastList(interaction, podcastList);
    }

    private async followRss(interaction: ChatInputCommandInteraction<CacheType>) {
        const feedUrl = interaction.options.getString('feed') || '';
        const podcast = await this.podcastHelpers.getPodcastFromUrl(feedUrl);
        const podcastList = !podcast ? [] : [podcast];

        this.followPodcastList(interaction, podcastList);
    }

    private unfollow(interaction: ChatInputCommandInteraction<CacheType>) {
        const feedId = interaction.options.getString('id') || '';
        const feed = this.podcastDataStorage.getFeedByFeedId(feedId);
        if (!feed) {
            this.sendNoMatchesMessageToChannel(interaction);
            return;
        }

        this.podcastDataStorage.removeFeed(feedId, interaction.channelId);
        const message = this.outgoingMessageFactory.buildUnfollowedMessage(
            feed.title,
            this.podcastDataStorage.getFeedsByChannelId(interaction.channelId),
        );
        interaction.editReply({ embeds: [message] });
    }

    private following(interaction: ChatInputCommandInteraction<CacheType>) {
        const feedList = this.podcastDataStorage.getFeedsByChannelId(interaction.channelId);
        const outgoingMessage = this.outgoingMessageFactory.buildFollowingMessage(feedList);
        // TODO: Split this into multiple messages/embeds so we don't have issues with too large of embeds
        // being created and not being able to be sent.
        interaction.editReply({ embeds: [outgoingMessage] });
    }

    private async help(interaction: ChatInputCommandInteraction<CacheType>) {
        const message = await this.outgoingMessageFactory.buildHelpMessage();
        interaction.editReply({ embeds: [message] });
    }

    private followPodcastList(
        interaction: ChatInputCommandInteraction<CacheType>,
        podcastList: Podcast[],
    ) {
        // TODO: This only supports following a list of quantity 1.
        if (podcastList.length !== 1) {
            this.sendNoMatchesMessageToChannel(interaction);
            return;
        }
        this.podcastDataStorage.addFeed(podcastList[0], interaction.channelId);
        const message = this.outgoingMessageFactory.buildFollowedMessage(
            podcastList[0],
            this.podcastDataStorage.getFeedsByChannelId(interaction.channelId),
        );
        interaction.editReply({ embeds: [message] });
    }

    private async sendMessageToChannel(
        channelId: string,
        embedBuilder: EmbedBuilder,
    ): Promise<void> {
        try {
            await this.discordMessageSender.send(channelId, embedBuilder);

            const memory = (process.memoryUsage().heapUsed / 1024 / 1024).toFixed(2) + 'MB';
            this.logger.info('Message Send Success', {
                title: embedBuilder.data.title,
                channelId,
                memory,
            });
        } catch (error) {
            this.logger.error('Message Send Failure', {
                message: embedBuilder.toJSON(),
                channelId,
                error,
            });
        }
        return;
    }

    private sendNoMatchesMessageToChannel(interaction: ChatInputCommandInteraction<CacheType>) {
        return interaction.editReply('Nothing was found matching your query.');
    }

    private sendErrorToChannel(interaction: ChatInputCommandInteraction<CacheType>) {
        const message =
            "Something went wrong. Check the bot's permissions, your input, and the podcast data.";
        return interaction.editReply(message);
    }
}

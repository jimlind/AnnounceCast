import { CacheType, ChatInputCommandInteraction, EmbedBuilder, Events } from 'discord.js';
import { Podcast } from 'podparse';
import DiscordConnection from './discord/discord-connection';
import DiscordMessageSender from './discord/discord-message-sender';
import HttpClient from './http-client';
import OutgoingMessageFactory from './outgoing-message/outgoing-message-factory';
import PodcastAppleAPIProcessor from './podcast/podcast-apple-api-processor';
import PodcastDataStorage from './podcast/podcast-data-storage';
import PodcastHelpers from './podcast/podcast-helpers';

interface BotInterface {
    readonly discordConnection: DiscordConnection;
    readonly discordEvents: Events;
    readonly discordMessageSender: DiscordMessageSender;
    readonly httpClient: HttpClient;
    readonly outgoingMessageFactory: OutgoingMessageFactory;
    readonly podcastAppleApiProcessor: PodcastAppleAPIProcessor;
    readonly podcastDataStorage: PodcastDataStorage;
    readonly podcastHelpers: PodcastHelpers;

    receiveInteraction(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class Bot implements BotInterface {
    constructor(
        readonly discordConnection: DiscordConnection,
        readonly discordEvents: Events,
        readonly discordMessageSender: DiscordMessageSender,
        readonly httpClient: HttpClient,
        readonly outgoingMessageFactory: OutgoingMessageFactory,
        readonly podcastAppleApiProcessor: PodcastAppleAPIProcessor,
        readonly podcastDataStorage: PodcastDataStorage,
        readonly podcastHelpers: PodcastHelpers,
    ) {}

    receiveInteraction(interaction: ChatInputCommandInteraction<CacheType>) {
        switch (interaction.commandName) {
            case 'search':
                this.search(interaction);
                break;
            case 'follow':
                this.follow(interaction);
                break;
            case 'follow-rss':
                this.followRss(interaction);
                break;
            case 'unfollow':
                this.unfollow(interaction);
                break;
            case 'following':
                this.following(interaction);
                break;
            default:
                this.help(interaction);
                break;
        }
    }

    async sendMostRecentPodcastEpisode(podcast: Podcast): Promise<void> {
        const outgoingMessage = this.outgoingMessageFactory.buildPodcastEpisodeMessage(podcast);

        const feedUrl = podcast.meta.importFeedUrl || '';
        const channelList = this.podcastDataStorage.getChannelsByFeedUrl(feedUrl);
        const mostRecentEpisode = this.podcastHelpers.getMostRecentPodcastEpisode(podcast);
        for (const channelId of channelList) {
            await this.sendMessageToChannel(channelId, outgoingMessage);
            this.podcastDataStorage.updatePostedData(feedUrl, mostRecentEpisode);
        }
    }

    private async search(interaction: ChatInputCommandInteraction<CacheType>) {
        const searchKeywords = interaction.options.getString('keywords') || '';
        const podcastList = await this.podcastAppleApiProcessor.search(searchKeywords, 4);

        // TODO: Catch if something went wrong.
        // this.logger.info(`Unable to search on term ${searchKeywords}`);
        // return this._sendErrorToChannel(commandInteraction);

        if (podcastList.length == 0) {
            interaction.editReply('No podcasts found.');
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

        // TODO: Log on Failure
        // this.logger.info(`Unable to follow on term ${searchKeywords}`);
        // return this._sendErrorToChannel(commandInteraction);
    }

    private async followRss(interaction: ChatInputCommandInteraction<CacheType>) {
        const feedUrl = interaction.options.getString('feed') || '';
        const podcastList = [];
        try {
            const podcast = await this.podcastHelpers.getPodcastFromUrl(feedUrl);
            podcastList.push(podcast);
        } catch (error) {
            // TODO: Log on Failure
            // this.logger.info(`Unable to follow on feed ${feedUrl}`);
            // return this._sendErrorToChannel(commandInteraction);
        }

        this.followPodcastList(interaction, podcastList);
    }

    private unfollow(interaction: ChatInputCommandInteraction<CacheType>) {
        const feedId = interaction.options.getString('id') || '';
        const feed = this.podcastDataStorage.getFeedByFeedId(feedId);
        if (!feed) {
            this.sendErrorToChannel(interaction);
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
        if (podcastList.length !== 1) {
            return this.sendErrorToChannel(interaction);
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
        await this.discordMessageSender.send(channelId, embedBuilder);

        // TODO: Log Success
        // const memory = (process.memoryUsage().heapUsed / 1024 / 1024).toFixed(2) + 'MB';
        // const data = { title: outgoingMessage.title, channelId, memory };
        // this.logger.info(`Message Send Success: ${JSON.stringify(data)}`);

        // TODO: Log Failure
        // const data = {
        //     message: outgoingMessage.toJSON(),
        //     channelId,
        //     error,
        // };
        // this.logger.error(`Message Send Failure: ${JSON.stringify(data)}`);

        return;
    }

    private sendErrorToChannel(interaction: ChatInputCommandInteraction<CacheType>) {
        const message =
            "Something went wrong. Check the bot's permissions, your input, and the podcast data.";
        return interaction.editReply(message);
    }
}

import { CacheType, ChatInputCommandInteraction } from 'discord.js';
import { Podcast } from 'podparse';
import DiscordMessageSender from '../discord/discord-message-sender.js';
import OutgoingMessageFactory from '../outgoing-message/outgoing-message-factory.js';
import PodcastDataStorage from '../podcast/podcast-data-storage.js';
import PodcastHelpers from '../podcast/podcast-helpers.js';

interface CommandHelpersInterface {
    readonly discordMessageSender: DiscordMessageSender;
    readonly outgoingMessageFactory: OutgoingMessageFactory;
    readonly podcastDataStorage: PodcastDataStorage;
    readonly podcastHelpers: PodcastHelpers;

    followPodcastList(
        interaction: ChatInputCommandInteraction<CacheType>,
        podcastList: Podcast[],
    ): void;
}

export default class CommandHelpers implements CommandHelpersInterface {
    constructor(
        readonly discordMessageSender: DiscordMessageSender,
        readonly outgoingMessageFactory: OutgoingMessageFactory,
        readonly podcastDataStorage: PodcastDataStorage,
        readonly podcastHelpers: PodcastHelpers,
    ) {}

    public async followPodcastList(
        interaction: ChatInputCommandInteraction<CacheType>,
        podcastList: Podcast[],
    ) {
        // TODO: This only supports following a list of quantity 1.
        if (podcastList.length !== 1) {
            await this.discordMessageSender.sendNoMatchesMessageAsReply(interaction);
            return;
        }
        this.podcastDataStorage.addFeed(podcastList[0], interaction.channelId);
        const message = this.outgoingMessageFactory.buildFollowedMessage(
            podcastList[0],
            this.podcastDataStorage.getFeedsByChannelId(interaction.channelId),
        );
        await interaction.editReply({ embeds: [message] });

        // Post most recent episode after follow has completed
        for (const podcast of podcastList) {
            // If the most recent episode is old only post to the channel the follow request came from
            const recentEpisodeIsOld = !this.podcastHelpers.mostRecentPodcastEpisodeIsNew(podcast);
            const channelInput = recentEpisodeIsOld ? interaction.channelId : '';
            await this.discordMessageSender.sendMostRecentPodcastEpisode(podcast, channelInput);
        }
    }
}

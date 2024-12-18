import { CacheType, ChatInputCommandInteraction } from 'discord.js';
import DiscordMessageSender from '../../discord/discord-message-sender.js';
import OutgoingMessageFactory from '../../outgoing-message/outgoing-message-factory.js';
import PodcastDataStorage from '../../podcast/podcast-data-storage.js';

interface UnfollowCommandInterface {
    readonly discordMessageSender: DiscordMessageSender;
    readonly outgoingMessageFactory: OutgoingMessageFactory;
    readonly podcastDataStorage: PodcastDataStorage;

    execute(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class UnfollowCommand implements UnfollowCommandInterface {
    constructor(
        readonly discordMessageSender: DiscordMessageSender,
        readonly outgoingMessageFactory: OutgoingMessageFactory,
        readonly podcastDataStorage: PodcastDataStorage,
    ) {}

    public async execute(interaction: ChatInputCommandInteraction<CacheType>) {
        const feedId = interaction.options.getString('id') || '';
        const feed = this.podcastDataStorage.getFeedByFeedId(feedId);
        if (!feed) {
            await this.discordMessageSender.sendNoMatchesMessageAsReply(interaction);
            await this.discordMessageSender.sendNoMatchesMessageAsReply(interaction);
            return;
        }

        this.podcastDataStorage.removeFeed(feedId, interaction.channelId);
        const message = this.outgoingMessageFactory.buildUnfollowedMessage(
            feed.title,
            this.podcastDataStorage.getFeedsByChannelId(interaction.channelId),
        );
        await interaction.editReply({ embeds: [message] });
    }
}

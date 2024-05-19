import { CacheType, ChatInputCommandInteraction, EmbedBuilder } from 'discord.js';
import PodcastFeedRow from '../../../models/db/podcast-feed-row.js';
import DiscordMessageSender from '../../discord/discord-message-sender.js';
import OutgoingMessageFactory from '../../outgoing-message/outgoing-message-factory.js';
import PodcastAppleAPIProcessor from '../../podcast/podcast-apple-api-processor.js';
import PodcastDataStorage from '../../podcast/podcast-data-storage.js';

interface FollowingCommandInterface {
    readonly discordMessageSender: DiscordMessageSender;
    readonly outgoingMessageFactory: OutgoingMessageFactory;
    readonly podcastAppleApiProcessor: PodcastAppleAPIProcessor;
    readonly podcastDataStorage: PodcastDataStorage;

    execute(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class FollowingCommand implements FollowingCommandInterface {
    constructor(
        readonly discordMessageSender: DiscordMessageSender,
        readonly outgoingMessageFactory: OutgoingMessageFactory,
        readonly podcastAppleApiProcessor: PodcastAppleAPIProcessor,
        readonly podcastDataStorage: PodcastDataStorage,
    ) {}

    public async execute(interaction: ChatInputCommandInteraction<CacheType>) {
        // Helper method to validate message is not null and send it
        const sendMessage = async (message: EmbedBuilder | null) => {
            if (message) {
                await this.discordMessageSender.sendMessageToChannel(
                    interaction.channelId,
                    message,
                );
            }
        };

        const feedList = this.podcastDataStorage.getFeedsByChannelId(interaction.channelId);
        interaction.deleteReply();

        const partialFeedList: PodcastFeedRow[] = [];
        let message = null;
        for (let index = 0; index < feedList.length; index++) {
            partialFeedList.push(feedList[index]);
            try {
                // Try to build the message and store it outside the loop for sending
                message = this.outgoingMessageFactory.buildFollowingMessage(partialFeedList);
            } catch (error) {
                const isSetDescriptionError =
                    error instanceof Error && error.stack?.includes('EmbedBuilder.setDescription');
                if (isSetDescriptionError) {
                    // Because building the message failed reset the list, undo increment, and send it
                    partialFeedList.length = 0;
                    index--;
                    await sendMessage(message);
                } else {
                    throw error;
                }
            }
        }
        // Send any lingering messages
        await sendMessage(message);
    }
}

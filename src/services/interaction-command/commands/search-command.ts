import { CacheType, ChatInputCommandInteraction } from 'discord.js';
import { Logger } from 'log4js';
import DiscordMessageSender from '../../discord/discord-message-sender.js';
import OutgoingMessageFactory from '../../outgoing-message/outgoing-message-factory.js';
import PodcastAppleAPIProcessor from '../../podcast/podcast-apple-api-processor.js';
import CommandHelpers from '../command-helpers.js';

interface SearchCommandInterface {
    readonly commandHelpers: CommandHelpers;
    readonly discordMessageSender: DiscordMessageSender;
    readonly logger: Logger;
    readonly outgoingMessageFactory: OutgoingMessageFactory;
    readonly podcastAppleApiProcessor: PodcastAppleAPIProcessor;

    execute(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class SearchCommand implements SearchCommandInterface {
    constructor(
        readonly commandHelpers: CommandHelpers,
        readonly discordMessageSender: DiscordMessageSender,
        readonly logger: Logger,
        readonly outgoingMessageFactory: OutgoingMessageFactory,
        readonly podcastAppleApiProcessor: PodcastAppleAPIProcessor,
    ) {}

    public async execute(interaction: ChatInputCommandInteraction<CacheType>) {
        const searchKeywords = interaction.options.getString('keywords') || '';
        const podcastList = await this.podcastAppleApiProcessor.search(searchKeywords, 4);
        if (podcastList.length == 0) {
            await this.commandHelpers.sendNoMatchesMessageAsReply(interaction);
            return;
        }

        const embedList = [];
        for (const podcast of podcastList) {
            const embed = await this.outgoingMessageFactory.buildPodcastInfoMessage(podcast);
            embedList.push(embed);
        }

        await interaction.editReply({ embeds: embedList });
    }
}

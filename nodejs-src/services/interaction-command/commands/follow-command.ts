import { CacheType, ChatInputCommandInteraction } from 'discord.js';
import PodcastAppleAPIProcessor from '../../podcast/podcast-apple-api-processor.js';
import CommandHelpers from '../command-helpers.js';

interface FollowCommandInterface {
    readonly commandHelpers: CommandHelpers;
    readonly podcastAppleApiProcessor: PodcastAppleAPIProcessor;

    execute(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class FollowCommand implements FollowCommandInterface {
    constructor(
        readonly commandHelpers: CommandHelpers,
        readonly podcastAppleApiProcessor: PodcastAppleAPIProcessor,
    ) {}

    public async execute(interaction: ChatInputCommandInteraction<CacheType>) {
        const searchKeywords = interaction.options.getString('keywords') || '';
        const podcastList = await this.podcastAppleApiProcessor.search(searchKeywords, 1);

        await this.commandHelpers.followPodcastList(interaction, podcastList);
    }
}

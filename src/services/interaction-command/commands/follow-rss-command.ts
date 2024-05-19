import { CacheType, ChatInputCommandInteraction } from 'discord.js';
import PodcastHelpers from '../../podcast/podcast-helpers.js';
import CommandHelpers from '../command-helpers.js';

interface FollowRssCommandInterface {
    readonly commandHelpers: CommandHelpers;
    readonly podcastHelpers: PodcastHelpers;

    execute(interaction: ChatInputCommandInteraction<CacheType>): void;
}

export default class FollowRssCommand implements FollowRssCommandInterface {
    constructor(
        readonly commandHelpers: CommandHelpers,
        readonly podcastHelpers: PodcastHelpers,
    ) {}

    public async execute(interaction: ChatInputCommandInteraction<CacheType>) {
        const feedUrl = interaction.options.getString('feed') || '';
        const podcast = await this.podcastHelpers.getPodcastFromUrl(feedUrl);
        const podcastList = !podcast ? [] : [podcast];

        await this.commandHelpers.followPodcastList(interaction, podcastList);
    }
}

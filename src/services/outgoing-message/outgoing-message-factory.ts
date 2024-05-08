import { EmbedBuilder } from 'discord.js';
import { Podcast } from 'podparse';
import PodcastFeedRow from '../../models/db/podcast-feed-row.js';
import EpisodeInfo from './messages/episode-info.js';
import Followed from './messages/followed.js';
import Following from './messages/following.js';
import Help from './messages/help.js';
import PodcastInfo from './messages/podcast-info.js';
import Unfollowed from './messages/unfollowed.js';

interface OutgoingMessageFactoryInterface {
    readonly discordEmbedBuilder: typeof EmbedBuilder;
    readonly episodeInfo: EpisodeInfo;
    readonly followed: Followed;
    readonly following: Following;
    readonly help: Help;
    readonly podcastInfo: PodcastInfo;
    readonly unfollowed: Unfollowed;

    buildFollowedMessage(podcast: Podcast, rowList: PodcastFeedRow[]): EmbedBuilder;
    buildUnfollowedMessage(title: string, rowList: PodcastFeedRow[]): EmbedBuilder;
    buildFollowingMessage(rowList: PodcastFeedRow[]): EmbedBuilder;
    buildHelpMessage(): Promise<EmbedBuilder>;
    buildPodcastEpisodeMessage(podcast: Podcast): EmbedBuilder;
    buildPodcastInfoMessage(podcast: Podcast): Promise<EmbedBuilder>;
}

export default class OutgoingMessageFactory implements OutgoingMessageFactoryInterface {
    readonly messageColor = 0x7ab87a;

    constructor(
        readonly discordEmbedBuilder: typeof EmbedBuilder,
        readonly episodeInfo: EpisodeInfo,
        readonly followed: Followed,
        readonly following: Following,
        readonly help: Help,
        readonly podcastInfo: PodcastInfo,
        readonly unfollowed: Unfollowed,
    ) {}

    public buildFollowedMessage(podcast: Podcast, rowList: PodcastFeedRow[]): EmbedBuilder {
        return this.followed.build(this.buildBaseMessage(), podcast, rowList);
    }

    public buildUnfollowedMessage(title: string, rowList: PodcastFeedRow[]): EmbedBuilder {
        return this.unfollowed.build(this.buildBaseMessage(), title, rowList);
    }

    public buildFollowingMessage(rowList: PodcastFeedRow[]): EmbedBuilder {
        return this.following.build(this.buildBaseMessage(), rowList);
    }

    public async buildHelpMessage(): Promise<EmbedBuilder> {
        return await this.help.build(this.buildBaseMessage());
    }

    public buildPodcastEpisodeMessage(podcast: Podcast): EmbedBuilder {
        return this.episodeInfo.build(this.buildBaseMessage(), podcast);
    }

    public async buildPodcastInfoMessage(podcast: Podcast): Promise<EmbedBuilder> {
        return this.podcastInfo.build(this.buildBaseMessage(), podcast);
    }

    private buildBaseMessage() {
        return new this.discordEmbedBuilder().setColor(this.messageColor);
    }
}

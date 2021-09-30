import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { PodcastFeedRow } from '../../models/db/podcast-feed-row';
import { Podcast } from '../../models/podcast';
import { Followed } from './messages/followed';
import { Following } from './messages/following';
import { Help } from './messages/help';
import { NewEpisode } from './messages/new-episode';
import { PodcastInfo } from './messages/podcast-info';

export class OutgoingMessageFactory {
    static [RESOLVER] = {}; // So Awilix autoloads the class
    // TODO: Create a new color and new icon for this bot.
    MESSAGE_COLOR = 0x7e4ea3;

    followed: Followed;
    following: Following;
    help: Help;
    newEpisode: NewEpisode;
    podcastInfo: PodcastInfo;

    constructor(
        followed: Followed,
        following: Following,
        help: Help,
        newEpisode: NewEpisode,
        podcastInfo: PodcastInfo,
    ) {
        this.followed = followed;
        this.following = following;
        this.help = help;
        this.newEpisode = newEpisode;
        this.podcastInfo = podcastInfo;
    }

    buildFollowedMessage(podcast: Podcast, rowList: PodcastFeedRow[]): MessageEmbed {
        const outgoingMessage = this.followed.build(this._buildBaseMessage(), podcast, rowList);

        return outgoingMessage;
    }

    buildUnfollowedMessage(title: string, rowList: PodcastFeedRow[]): MessageEmbed {
        const outgoingMessage = this.following.build(this._buildBaseMessage(), rowList);
        outgoingMessage.setTitle('Unfollow Successful');
        outgoingMessage.description =
            `You are no longer following **${title}**\n` + outgoingMessage.description;

        return outgoingMessage;
    }

    buildFollowingMessage(rowList: PodcastFeedRow[]): MessageEmbed {
        return this.following.build(this._buildBaseMessage(), rowList);
    }

    buildHelpMessage(): MessageEmbed {
        return this.help.build(this._buildBaseMessage());
    }

    buildNewEpisodeMessage(podcast: Podcast): MessageEmbed {
        return this.newEpisode.build(this._buildBaseMessage(), podcast);
    }

    buildPodcastInfoMessage(podcast: Podcast): MessageEmbed {
        return this.podcastInfo.build(this._buildBaseMessage(), podcast);
    }

    _buildBaseMessage(): MessageEmbed {
        const message = new MessageEmbed().setColor(this.MESSAGE_COLOR);
        return message;
    }
}

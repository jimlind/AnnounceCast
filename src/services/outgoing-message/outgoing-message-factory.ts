import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { PodcastFeedRow } from '../../models/db/podcast-feed-row';
import { Podcast } from '../../models/podcast';
import { Following } from './messages/following';
import { Help } from './messages/help';
import { NewEpisode } from './messages/new-episode';

export class OutgoingMessageFactory {
    static [RESOLVER] = {}; // So Awilix autoloads the class
    MESSAGE_COLOR = 0x7e4ea3;

    following: Following;
    help: Help;
    newEpisode: NewEpisode;

    constructor(following: Following, help: Help, newEpisode: NewEpisode) {
        this.following = following;
        this.help = help;
        this.newEpisode = newEpisode;
    }

    buildFollowedMessage(podcast: Podcast, rowList: PodcastFeedRow[]): MessageEmbed {
        const outgoingMessage = this.following.build(this._buildBaseMessage(), rowList);
        outgoingMessage.setTitle('Follow Successful');
        outgoingMessage.description =
            `You are now following **${podcast.title}**\n` + outgoingMessage.description;

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

    buildNewEpisodeMessage(podcast: Podcast): MessageEmbed {
        return this.newEpisode.build(this._buildBaseMessage(), podcast);
    }

    _buildBaseMessage(): MessageEmbed {
        const message = new MessageEmbed().setColor(this.MESSAGE_COLOR);
        return message;
    }
}

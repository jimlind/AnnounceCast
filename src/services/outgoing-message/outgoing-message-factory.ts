import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { PodcastFeedRow } from '../../models/db/podcast-feed-row';
import { Podcast } from '../../models/podcast';
import { Following } from './messages/following';
import { Help } from './messages/help';
import { InadequatePermissions } from './messages/inadequate-permissions';
import { NewEpisode } from './messages/new-episode';

export class OutgoingMessageFactory {
    static [RESOLVER] = {}; // So Awilix autoloads the class
    MESSAGE_COLOR = 0x7e4ea3;

    following: Following;
    help: Help;
    inadequatePermissions: InadequatePermissions;
    newEpisode: NewEpisode;

    constructor(
        following: Following,
        help: Help,
        inadequatePermissions: InadequatePermissions,
        newEpisode: NewEpisode,
    ) {
        this.following = following;
        this.help = help;
        this.inadequatePermissions = inadequatePermissions;
        this.newEpisode = newEpisode;
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

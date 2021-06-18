import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { PodcastFeedRow } from '../../models/db/podcast-feed-row';
import { Following } from './messages/following';
import { Help } from './messages/help';
import { InadequatePermissions } from './messages/inadequate-permissions';
import { PodcastEpisode } from './messages/podcast-episode';

export class OutgoingMessageFactory {
    static [RESOLVER] = {}; // So Awilix autoloads the class
    MESSAGE_COLOR = 0x7e4ea3;

    following: Following;
    help: Help;
    inadequatePermissions: InadequatePermissions;
    podcastEpisode: PodcastEpisode;

    constructor(
        following: Following,
        help: Help,
        inadequatePermissions: InadequatePermissions,
        podcastEpisode: PodcastEpisode,
    ) {
        this.following = following;
        this.help = help;
        this.inadequatePermissions = inadequatePermissions;
        this.podcastEpisode = podcastEpisode;
    }

    _baseMessage(): MessageEmbed {
        const message = new MessageEmbed().setColor(this.MESSAGE_COLOR);
        return message;
    }

    buildFollowingMessage(rowList: PodcastFeedRow[]): MessageEmbed {
        return this.following.build(this._baseMessage(), rowList);
    }
}

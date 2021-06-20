import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Podcast } from '../../../models/podcast';

export class PodcastInfo {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    build(message: MessageEmbed, podcast: Podcast): MessageEmbed {
        message.setTitle(podcast.title);
        message.setThumbnail(podcast.image);
        message.setDescription(
            `Show Feed URL: ${podcast.feed}` + '\n' + `Show's Website: ${podcast.link}`,
        );
        message.setFooter(`Credit: ${podcast.author}`);

        return message;
    }
}

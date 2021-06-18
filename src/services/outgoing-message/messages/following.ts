import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { PodcastFeedRow } from '../../../models/db/podcast-feed-row';

export class Following {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    build(message: MessageEmbed, rows: PodcastFeedRow[]): MessageEmbed {
        message.setTitle('Podcasts Followed in this Channel');

        const feeds: Array<string> = ['ID     / TITLE'];
        rows.forEach((row) => {
            feeds.push(`${row.id} / ${row.title}`);
        });
        message.setDescription('```\n' + feeds.join('\n') + '\n```');

        return message;
    }
}

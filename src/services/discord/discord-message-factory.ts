import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Podcast } from '../../models/podcast.js';

export class DiscordMessageFactory {
    static [RESOLVER] = {};

    buildFollowingMessage(rows: Array<any>): MessageEmbed {
        const message = new MessageEmbed().setColor(0x7e4ea3);
        message.setTitle('Podcasts Followed in this Channel');

        const feeds: Array<string> = ['ID     / TITLE'];
        rows.forEach((row) => {
            feeds.push(`${row.id} / ${row.title}`);
        });
        message.setDescription('```\n' + feeds.join('\n') + '\n```');

        return message;
    }

    buildEpisodeMessage(podcast: Podcast): MessageEmbed {
        const message = new MessageEmbed().setColor(0x7e4ea3);

        message.setAuthor(podcast.showTitle, podcast.showImage, podcast.showLink);
        message.setTitle(podcast.episodeTitle);
        message.setURL(podcast.episodeLink);
        message.setDescription(podcast.episodeDescription);
        message.setImage(podcast.episodeImage || podcast.showImage);

        message.setFooter(this.createFooterText(podcast));

        return message;
    }

    createFooterText(podcast: Podcast): string {
        const footerData = [];

        let episodeText = '';
        episodeText += podcast.seasonNumber ? `S${podcast.seasonNumber}` : '';
        episodeText += podcast.seasonNumber && podcast.episodeNumber ? ':' : '';
        episodeText += podcast.episodeNumber ? `E${podcast.episodeNumber}` : '';
        footerData.push(episodeText);

        footerData.push(podcast.episodeDuration);
        footerData.push(podcast.episodeExplicit ? 'Parental Advisory - Explicit Content' : '');

        return footerData.filter(Boolean).join(' | ');
    }
}

import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Config } from '../../models/config.js';
import { Podcast } from '../../models/podcast.js';
import { DiscordDataStorage } from './discord-data-storage.js';

export class DiscordMessageFactory {
    static [RESOLVER] = {};

    config: Config;
    discordDataStorage: DiscordDataStorage;

    constructor(config: Config, discordDataStorage: DiscordDataStorage) {
        this.config = config;
        this.discordDataStorage = discordDataStorage;
    }

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

    buildHelpMessage(guildId: string): MessageEmbed {
        const prefix = this.discordDataStorage.getPrefix(guildId);

        const message = new MessageEmbed().setColor(0x7e4ea3);
        message.setTitle(`${this.config.appName} v${this.config.appVersion} [ prefix: ${prefix} ]`);
        message.setURL('https://github.com/jimlind/discord.podcasts');
        message.addField('?podcasts', 'View this help message.');
        message.addField(
            '?podcasts prefix <value> 🔒',
            'Set the bots prefix with the string <value> argument.',
        );
        message.addField(
            `${prefix}following`,
            'Display all podcasts bot is following in this channel.',
        );
        message.addField(
            `${prefix}follow <url> 🔒`,
            'Follow a podcast in this channel with the feed URL <url> argument.',
        );
        message.addField(
            `${prefix}unfollow <id> 🔒`,
            'Unfollow a podcast in this channel with the following ID <id> argument',
        );
        message.addField(
            `${prefix}play <id>`,
            'Play the most recent episode of a podcast in your voice channel with the following ID <id> argument',
        );
        message.setFooter(
            'The 🔒 commands are only available to users with Manage Server permissions.',
        );

        return message;
    }

    buildInadequatePermissionsMessage(): MessageEmbed {
        const message = new MessageEmbed().setColor(0x7e4ea3);
        message.setDescription(
            'Only users with Manage Server permissions can perform that action.',
        );

        return message;
    }
}

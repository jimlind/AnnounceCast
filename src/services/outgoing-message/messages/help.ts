import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Config } from '../../../models/config';
import { DiscordConnection } from '../../discord/discord-connection';
import { PodcastDataStorage } from '../../podcast/podcast-data-storage';

export class Help {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    config: Config;
    discordConnection: DiscordConnection;
    podcastDataStorage: PodcastDataStorage;
    constructor(
        config: Config,
        discordConnection: DiscordConnection,
        podcastDataStorage: PodcastDataStorage,
    ) {
        this.config = config;
        this.discordConnection = discordConnection;
        this.podcastDataStorage = podcastDataStorage;
    }

    build(message: MessageEmbed) {
        const feedCount = this.podcastDataStorage.getFeedCount();
        const serverCount = this.discordConnection.getClient().guilds.cache.size;

        message.setTitle(`${this.config.appName} v${this.config.appVersion}`);
        message.setURL('https://github.com/jimlind/discord.podcasts');
        message.setDescription(`Tracking ${feedCount} podcasts on ${serverCount} servers.`);

        message.addField('/help', '> View this help message.');
        message.addField(
            '/find <keywords>',
            '> Replies with up to 4 podcasts matching the search keyword(s)',
        );
        message.addField(
            '/following',
            '> Replies with the list of all podcasts (Ids & Names) followed in this channel',
        );
        message.addField(
            '/follow <keywords> 🔒',
            '> Follow a podcast in this channel matching the search keyword(s)',
        );
        message.addField(
            '/follow-rss <feed> 🔒',
            '> Follow a podcast in this channel using an RSS feed',
        );
        message.addField(
            '/unfollow <id> 🔒',
            '> Unfollow a podcast in this channel using the Podcast Id',
        );
        message.addField(
            '/play <id>',
            '> Play the most recent episode of a podcast using the Podcast Id',
        );
        message.setFooter(
            'The 🔒 commands are only available to users with Manage Server permissions.',
        );

        return message;
    }
}

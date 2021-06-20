import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Config } from '../../../models/config';
import { DiscordConnection } from '../../discord/discord-connection';
import { DiscordDataStorage } from '../../discord/discord-data-storage';
import { PodcastDataStorage } from '../../podcast/podcast-data-storage';

export class Help {
    static [RESOLVER] = {}; // So Awilix autoloads the class

    config: Config;
    discordConnection: DiscordConnection;
    discordDataStorage: DiscordDataStorage;
    podcastDataStorage: PodcastDataStorage;
    constructor(
        config: Config,
        discordDataStorage: DiscordDataStorage,
        discordConnection: DiscordConnection,
        podcastDataStorage: PodcastDataStorage,
    ) {
        this.config = config;
        this.discordDataStorage = discordDataStorage;
        this.discordConnection = discordConnection;
        this.podcastDataStorage = podcastDataStorage;
    }

    build(message: MessageEmbed, guildId: string) {
        const prefix = this.discordDataStorage.getPrefix(guildId);
        const feedCount = this.podcastDataStorage.getFeedCount();
        const serverCount = this.discordConnection.getClient().guilds.cache.size;

        message.setTitle(
            `${this.config.appName} v${this.config.appVersion} (prefix: \`${prefix}\` )`,
        );
        message.setURL('https://github.com/jimlind/discord.podcasts');

        message.setDescription(`Tracking ${feedCount} podcasts on ${serverCount} servers.`);

        message.addField('?podcasts', '> View this help message.');
        message.addField(
            '?podcasts prefix <value> 🔒 ',
            "> Set the bot's custom prefix with the string <value> argument.",
        );
        message.addField(
            `${prefix}find <search terms>`,
            '> Displays up to 4 podcasts matching the <search terms>',
        );
        message.addField(
            `${prefix}following`,
            '> Display the podcasts (ids and names) followed in this channel.',
        );
        message.addField(
            `${prefix}follow <url> 🔒`,
            '> Follow a podcast in this channel with the feed URL <url> argument.',
        );
        message.addField(
            `${prefix}unfollow <id> 🔒`,
            '> Unfollow a podcast with the podcast id <id> argument.',
        );
        message.addField(
            `${prefix}play <id>`,
            '> Play the most recent episode of a podcast with the podcast id <id> argument.',
        );
        message.setFooter(
            'The 🔒 commands are only available to users with Manage Server permissions.',
        );

        return message;
    }
}

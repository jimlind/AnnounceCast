import { RESOLVER } from 'awilix';
import { MessageEmbed } from 'discord.js';
import { Config } from '../../models/config.js';
import { PodcastFeedRow } from '../../models/db/podcast-feed-row.js';
import { PodcastEpisode } from '../../models/podcast-episode.js';
import { OutgoingMessageFactory } from '../outgoing-message/outgoing-message-factory.js';
import { PodcastDataStorage } from '../podcast/podcast-data-storage.js';
import { DiscordConnection } from './discord-connection.js';
import { DiscordDataStorage } from './discord-data-storage.js';

// This class probably ends up being replaced by the the OutgoingMessageFactory class completely.
export class DiscordMessageFactory {
    static [RESOLVER] = {};

    config: Config;
    discordConnection: DiscordConnection;
    discordDataStorage: DiscordDataStorage;
    podcastDataStorage: PodcastDataStorage;
    outgoingMessageFactory: OutgoingMessageFactory;

    constructor(
        config: Config,
        discordConnection: DiscordConnection,
        discordDataStorage: DiscordDataStorage,
        podcastDataStorage: PodcastDataStorage,
        outgoingMessageFactory: OutgoingMessageFactory,
    ) {
        this.config = config;
        this.discordConnection = discordConnection;
        this.discordDataStorage = discordDataStorage;
        this.podcastDataStorage = podcastDataStorage;
        this.outgoingMessageFactory = outgoingMessageFactory;
    }

    buildMessage(): MessageEmbed {
        const message = new MessageEmbed().setColor(0x7e4ea3);
        return message;
    }

    buildHelpMessage(guildId: string): MessageEmbed {
        const prefix = this.discordDataStorage.getPrefix(guildId);
        const feedCount = this.podcastDataStorage.getFeedCount();
        const serverCount = this.discordConnection.getClient().guilds.cache.size;

        const message = new MessageEmbed().setColor(0x7e4ea3);
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

    buildInadequatePermissionsMessage(): MessageEmbed {
        const message = new MessageEmbed().setColor(0x7e4ea3);
        message.setDescription(
            'Only users with Manage Server permissions can perform that action.',
        );

        return message;
    }
}

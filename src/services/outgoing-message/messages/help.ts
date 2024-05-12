interface HelpInterface {
    readonly config: typeof import('../../../config.js').default;
    readonly discordConnection: import('../../discord/discord-connection.js').default;
    readonly podcastDataStorage: import('../../podcast/podcast-data-storage.js').default;

    build(
        embedBuilder: import('discord.js').EmbedBuilder,
    ): Promise<import('discord.js').EmbedBuilder>;
}

export default class Help implements HelpInterface {
    constructor(
        readonly config: typeof import('../../../config.js').default,
        readonly discordConnection: import('../../discord/discord-connection.js').default,
        readonly podcastDataStorage: import('../../podcast/podcast-data-storage.js').default,
    ) {}

    public async build(embedBuilder: import('discord.js').EmbedBuilder) {
        const discordClient = await this.discordConnection.getClient();
        const serverCount = discordClient.guilds.cache.size;
        const feedCount = this.podcastDataStorage.getFeedCount();

        embedBuilder.setTitle(`${this.config.app.name} v${this.config.app.version} Documentation`);
        embedBuilder.setURL('https://jimlind.github.io/AnnounceCast/');
        embedBuilder.setDescription(`Tracking ${feedCount} podcasts on ${serverCount} servers.`);
        embedBuilder.addFields(
            {
                name: '/follow <keywords> 🔒',
                value: 'Follow a podcast in the channel matching the search keyword(s)',
            },
            {
                name: '/follow-rss <feed> 🔒',
                value: 'Follow a podcast in the channel using an RSS feed',
            },
            {
                name: '/unfollow <id> 🔒',
                value: 'Unfollow a podcast in the channel using a Podcast Id',
            },
            {
                name: '/following',
                value: 'Display a list of all podcasts followed in the channel',
            },
            {
                name: '/search <keywords>',
                value: 'Display up to 4 podcasts matching the search keyword(s)',
            },
            {
                name: '/help [test]',
                value: 'Display this help message, optionally sending test messages',
            },
        );
        embedBuilder.addFields(
            {
                name: ':clap: Patreon',
                value: '[Support on Patreon](https://www.patreon.com/AnnounceCast)',
                inline: true,
            },
            {
                name: ':left_speech_bubble: Discord',
                value: '[Join the Discord](https://discord.gg/sEjJTTjG3M)',
                inline: true,
            },
        );
        embedBuilder.setFooter({
            text: 'The commands marked with 🔒 default to manager only visibility but permissions can be overridden by admins.',
        });

        return embedBuilder;
    }
}

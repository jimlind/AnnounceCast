interface HelpInterface {
    readonly config: typeof import('../../../config').default;
    readonly discordEmbedBuilder: typeof import('discord.js').EmbedBuilder;
    readonly discordConnection: import('../../discord/discord-connection').default;
    readonly podcastDataStorage: import('../../podcast/podcast-data-storage').default;

    build(
        embedBuilder: import('discord.js').EmbedBuilder,
    ): Promise<import('discord.js').EmbedBuilder>;
}

export default class Help implements HelpInterface {
    constructor(
        readonly config: typeof import('../../../config').default,
        readonly discordEmbedBuilder: typeof import('discord.js').EmbedBuilder,
        readonly discordConnection: import('../../discord/discord-connection').default,
        readonly podcastDataStorage: import('../../podcast/podcast-data-storage').default,
    ) {}

    async build(embedBuilder: import('discord.js').EmbedBuilder) {
        const discordClient = await this.discordConnection.getClient();
        const serverCount = discordClient.guilds.cache.size;
        const feedCount = this.podcastDataStorage.getFeedCount();

        embedBuilder.setTitle(`${this.config.app.name} v${this.config.app.version} Documentation`);
        embedBuilder.setURL('https://jimlind.github.io/AnnounceCast/');
        embedBuilder.setDescription(`Tracking ${feedCount} podcasts on ${serverCount} servers.`);
        embedBuilder.addFields(
            { name: '/help', value: 'View this help message' },
            {
                name: '/find <keywords>',
                value: 'Replies with up to 4 podcasts matching the search keyword(s)',
            },
            {
                name: '/following',
                value: 'Replies with the list of all podcasts (Ids & Names) followed in this channel',
            },
            {
                name: '/follow <keywords> 🔒',
                value: 'Follow a podcast in this channel matching the search keyword(s)',
            },
            {
                name: '/follow-rss <feed> 🔒',
                value: 'Follow a podcast in this channel using an RSS feed',
            },
            {
                name: '/unfollow <id> 🔒',
                value: 'Unfollow a podcast in this channel using the Podcast Id',
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

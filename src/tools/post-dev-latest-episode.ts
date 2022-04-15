import { Container } from '../container.js';
import { Bot } from '../services/bot.js';
import { DiscordConnection } from '../services/discord/discord-connection.js';
import { PodcastRssProcessor } from '../services/podcast/podcast-rss-processor.js';

const feedUrl = 'https://anchor.fm/s/133d445c/podcast/rss';

const container: Container = new Container('dev');
container.register().then(() => {
    container
        .resolve<PodcastRssProcessor>('podcastRssProcessor')
        .process(feedUrl, 1)
        .then((podcast) => {
            return container.resolve<Bot>('bot').sendNewEpisodeAnnouncement(podcast);
        })
        .then(() => {
            // This is a bit silly but registering the container created the client so we need to destroy it
            container.resolve<DiscordConnection>('discordConnection').getClient().destroy();
            console.log('✅  Complete');
        });
});

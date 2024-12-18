/**
 * Run this command in the terminal for prod or dev like this:
 * >>> NODE_ENV=production node --import ./register.mjs ./src/tools/maintenance/post-all-latest.ts
 * >>> NODE_ENV=development node --import ./register.mjs ./src/tools/maintenance/post-all-latest.ts
 */

import bettersqlite3 from 'better-sqlite3';
import { Container } from '../../container.js';
import DiscordConnection from '../../services/discord/discord-connection.js';
import DiscordMessageSender from '../../services/discord/discord-message-sender.js';
import PodcastHelpers from '../../services/podcast/podcast-helpers.js';

try {
    const container = new Container();
    await run(container);
} catch (error) {
    console.log('❌ Unable to run command');
    console.log(error);
}

async function run(container: Container) {
    await container.register();

    const betterSqlite3 = container.resolve<typeof bettersqlite3>('betterSqlite3');
    const discordMessageSender = container.resolve<DiscordMessageSender>('discordMessageSender');
    const podcastHelpers = container.resolve<PodcastHelpers>('podcastHelpers');

    const database = betterSqlite3('./db/podcasts.db');
    const feedUrlList = database.prepare('SELECT url FROM feeds').pluck().all().map(String);

    for (const feedUrl of feedUrlList) {
        const podcast = await podcastHelpers.getPodcastFromUrl(feedUrl);
        await discordMessageSender.sendMostRecentPodcastEpisode(podcast);
    }

    database.close();

    const discordConnection = container.resolve<DiscordConnection>('discordConnection');
    const discordClient = await discordConnection.getClient();
    discordClient.destroy();
}

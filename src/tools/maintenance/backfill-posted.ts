/**
 * Run this command in the terminal against the local database like this:
 * >>> node --import ./register.mjs ./src/tools/maintenance/backfill-posted.ts
 */

import bettersqlite3 from 'better-sqlite3';
import { Container } from '../../container.js';
import PodcastDataStorage from '../../services/podcast/podcast-data-storage.js';
import PodcastHelpers from '../../services/podcast/podcast-helpers.js';

try {
    console.log('--------------------');
    const container = new Container();
    await run(container);
    console.log('✅ Command completed');
} catch (error) {
    console.log('❌ Unable to run command');
    console.log(error);
}

async function run(container: Container) {
    await container.register();
    const betterSqlite3 = container.resolve<typeof bettersqlite3>('betterSqlite3');
    const podcastDataStorage = container.resolve<PodcastDataStorage>('podcastDataStorage');
    const podcastHelpers = container.resolve<PodcastHelpers>('podcastHelpers');

    const database = betterSqlite3('./db/podcasts.db');
    const rows = database.prepare('SELECT id, url FROM feeds').all().map(Object);

    for (const row of rows) {
        const podcast = await podcastHelpers.getPodcastFromUrl(row?.url || '');
        const episodeList = podcast.episodes
            .sort((a, b) => {
                return new Date(b.pubDate).getTime() - new Date(a.pubDate).getTime();
            })
            .slice(0, 6)
            .reverse();

        if (episodeList.length < 6) {
            continue;
        }

        for (const episode of episodeList) {
            podcastDataStorage.updatePostedData(podcast.meta.importFeedUrl || '', episode);
        }
    }

    database.close();
    podcastDataStorage.close();
}

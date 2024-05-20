/**
 * Run this command in the terminal against the local database like this:
 * >>> node --loader ts-node/esm src/tools/maintenance/delete-bad-feeds.ts
 */

import bettersqlite3 from 'better-sqlite3';
import fs from 'fs';
import { Container } from '../../container.js';
import PodcastHelpers from '../../services/podcast/podcast-helpers.js';

const outputFile = `./src/tools/maintenance/output/deleted-bad-feeds-${Date.now()}.csv`;
const dryRun = true;

try {
    console.log('--------------------');
    const container = new Container();
    await run(container);
    console.log('✅ Command completed');
} catch (error) {
    console.log('❌ Unable to run command');
    console.log(error);
}
process.exit();

async function run(container: Container) {
    await container.register();
    const betterSqlite3 = container.resolve<typeof bettersqlite3>('betterSqlite3');
    const podcastHelpers = container.resolve<PodcastHelpers>('podcastHelpers');

    const database = betterSqlite3('./db/podcasts.db');
    let urlList = database.prepare('SELECT url FROM feeds').pluck().all().map(String);

    for (let index = 0; index < 10; index++) {
        process.stdout.write(`${urlList.length} podcasts to check - `);

        const badUrlList = [];
        for (const url of urlList) {
            if (!(await isGoodFeedUrl(String(url), podcastHelpers))) {
                badUrlList.push(url);
            }
        }
        urlList = badUrlList;
        process.stdout.write('\n');
    }

    purgeFeeds(urlList, database);
    database.close();
}

async function isGoodFeedUrl(url: string, podcastHelpers: PodcastHelpers): Promise<boolean> {
    try {
        const podcast = await podcastHelpers.getPodcastFromUrl(url);
        if (!podcast.episodes.length) {
            throw new Error('No Episodes in Podcast');
        }
        process.stdout.write('.');
        return true;
    } catch (error) {
        process.stdout.write('✗');
    }
    return false;
}

async function purgeFeeds(urlList: string[], database: bettersqlite3.Database) {
    for (const url of urlList) {
        const feedId = database.prepare('SELECT id FROM feeds WHERE url = ?').pluck().get(url);
        const channelList = database
            .prepare('SELECT channel_id FROM channels WHERE feed_id = ?')
            .pluck()
            .all(feedId)
            .map(String);
        fs.appendFileSync(outputFile, `"${url}","${channelList.join('|')}"\n`, 'utf-8');

        let postedResult, channelsResult, feedsResult;
        if (!dryRun) {
            postedResult = database.prepare('DELETE FROM posted WHERE feed_id = ?').run(feedId);
            channelsResult = database.prepare('DELETE FROM channels WHERE feed_id = ?').run(feedId);
            feedsResult = database.prepare('DELETE FROM feeds WHERE id = ?').run(feedId);
        }

        const changes =
            (postedResult?.changes || 0) +
            (channelsResult?.changes || 0) +
            (feedsResult?.changes || 0);
        console.log(`🚮 ${changes}x changes for feed ${feedId}`);
    }
}

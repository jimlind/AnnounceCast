/**
 * Run this command in the terminal against the local database like this:
 * >>> node --import ./register.mjs ./src/tools/maintenance/delete-bad-feeds.ts
 */

import { confirm } from '@inquirer/prompts';
import bettersqlite3 from 'better-sqlite3';
import fs from 'fs';
import { Logger } from 'log4js';
import { Container } from '../../container.js';
import PodcastHelpers from '../../services/podcast/podcast-helpers.js';

const outputFolder = './src/tools/maintenance/output';
const outputFile = `${outputFolder}/deleted-bad-feeds-${Date.now()}.csv`;

try {
    fs.mkdirSync(outputFolder);
} catch (e) {
    // Do nothing output folder exists
}

try {
    console.log('--------------------');
    const container = new Container();

    const logger = container.resolve<Logger>('logger');
    logger.level = 'warn';

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
    let badUrlList = [];

    for (let index = 0; index < 10; index++) {
        process.stdout.write(`${urlList.length} podcasts to check - `);

        badUrlList = [];
        for (const url of urlList) {
            const podcastError = await getPodcastError(String(url), podcastHelpers);
            if (podcastError) {
                badUrlList.push({ url, error: podcastError });
            }
        }
        urlList = badUrlList.map((x) => x.url);
        process.stdout.write('\n');
    }

    await purgeFeeds(badUrlList, database);
    database.close();
}

async function getPodcastError(url: string, podcastHelpers: PodcastHelpers): Promise<string> {
    let errorText = '';
    try {
        const podcast = await podcastHelpers.getPodcastFromUrl(url);
        if (!podcast.episodes.length) {
            throw new Error('No Episodes in Podcast');
        }
        process.stdout.write('.');
        return '';
    } catch (error) {
        errorText = error instanceof Error ? error.message : 'Unknown problem with podcast.';
        process.stdout.write('✗');
    }

    return errorText;
}

async function purgeFeeds(
    badUrlList: { url: string; error: string }[],
    database: bettersqlite3.Database,
) {
    // Prompt for actual deletes
    const live = await confirm({ message: 'Perform actual deletes?' });

    for (const badUrlData of badUrlList) {
        const feedId = database
            .prepare('SELECT id FROM feeds WHERE url = ?')
            .pluck()
            .get(badUrlData.url);
        const channelList = database
            .prepare('SELECT channel_id FROM channels WHERE feed_id = ?')
            .pluck()
            .all(feedId)
            .map(String);
        fs.appendFileSync(outputFile, `"${badUrlData.url}","${channelList.join('|')}"\n`, 'utf-8');
        process.stdout.write(`The problem with ${badUrlData.url} is "${badUrlData.error}"\n`);

        let postedResult, channelsResult, feedsResult;
        if (live) {
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

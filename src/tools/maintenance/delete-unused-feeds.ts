/**
 * Run this command in the terminal against the local database like this:
 * >>> node --import ./register.mjs ./src/tools/maintenance/delete-unused-feeds.ts
 */

import bettersqlite3 from 'better-sqlite3';
import fs from 'fs';
import { Container } from '../../container.js';

const outputFolder = './src/tools/maintenance/output';
const outputFile = `${outputFolder}/deleted-unused-feeds-${Date.now()}.csv`;
const dryRun = true;

try {
    fs.mkdirSync(outputFolder);
} catch (e) {
    // Do nothing output folder exists
}

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

    // Select feed URLs that don't have entries in the channels table
    // If nobody is following then we should stop scraping
    const database = betterSqlite3('./db/podcasts.db');
    const query =
        'SELECT url FROM feeds f LEFT JOIN channels c ON f.id = c.feed_id WHERE c.feed_id IS NULL';
    const urlList = database.prepare(query).pluck().all().map(String);

    purgeFeeds(urlList, database);
    database.close();
}

async function purgeFeeds(urlList: string[], database: bettersqlite3.Database) {
    for (const url of urlList) {
        const feedId = database.prepare('SELECT id FROM feeds WHERE url = ?').pluck().get(url);
        fs.appendFileSync(outputFile, `"${url}"\n`, 'utf-8');

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

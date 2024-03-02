#!/usr/bin/env ts-node
/**
 * Run this command in the terminal for against the local database like this:
 * >>> node --loader ts-node/esm src/tools/maintenance/delete-bad-feeds.ts
 */

import bettersqlite3 from 'better-sqlite3';
import { Container } from '../../container.js';
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
    const podcastHelpers = container.resolve<PodcastHelpers>('podcastHelpers');

    const database = betterSqlite3('./db/podcasts.db');
    const badFeedIdList = await getBadFeedIdList(database, podcastHelpers);
    for (const feedId of badFeedIdList) {
        deleteBadData(database, feedId);
    }

    database.close();
}

async function getBadFeedIdList(
    database: bettersqlite3.Database,
    podcastHelpers: PodcastHelpers,
): Promise<string[]> {
    const rows = database.prepare('SELECT id, url FROM feeds').all().map(Object);
    const badFeedIdList = [];
    for (const row of rows) {
        try {
            const podcast = await podcastHelpers.getPodcastFromUrl(row?.url || '');
            if (!podcast.episodes.length) {
                throw new Error('No Episodes in Podcast');
            }
        } catch (error) {
            badFeedIdList.push(row?.id || '');
        }
    }
    return badFeedIdList;
}

function deleteBadData(database: bettersqlite3.Database, feedId: string) {
    const postedResult = database.prepare('DELETE FROM posted WHERE feed_id = ?').run(feedId);
    const channelsResult = database.prepare('DELETE FROM channels WHERE feed_id = ?').run(feedId);
    const feedsResult = database.prepare('DELETE FROM feeds WHERE id = ?').run(feedId);

    const changes = postedResult.changes + channelsResult.changes + feedsResult.changes;
    console.log(`🚮 ${changes}x changes for feed ${feedId}`);
}

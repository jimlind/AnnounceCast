#!/usr/bin/env ts-node
/**
 * Run this command in the terminal to update the local database like this:
 * >>> node --loader ts-node/esm src/tools/maintenance/add-feed.ts
 */

import { stdin as input, stdout as output } from 'node:process';
import * as readline from 'node:readline/promises';
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
    let feedUrl = 'https://anchor.fm/s/238d77c8/podcast/rss';
    let channelId = '1203413874183774290';

    await container.register();
    const podcastHelpers = container.resolve<PodcastHelpers>('podcastHelpers');
    const podcastDataStorage = container.resolve<PodcastDataStorage>('podcastDataStorage');
    const readlineInterface = readline.createInterface({ input, output });

    readlineInterface.write(feedUrl);
    feedUrl = await readlineInterface.question('Podcast Feed URL: ');
    readlineInterface.write(channelId);
    channelId = await readlineInterface.question('Discord Channel ID: ');
    readlineInterface.close();

    const podcast = await podcastHelpers.getPodcastFromUrl(feedUrl);
    podcastDataStorage.addFeed(podcast, channelId);
}

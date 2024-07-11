/**
 * Run this command in the terminal to update the local database like this:
 * >>> node --loader ts-node/esm src/tools/maintenance/add-feed.ts
 */

import inquirer from 'inquirer';
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
    const podcastHelpers = container.resolve<PodcastHelpers>('podcastHelpers');
    const podcastDataStorage = container.resolve<PodcastDataStorage>('podcastDataStorage');

    const questions = [
        {
            type: 'input',
            name: 'feedUrl',
            message: 'Enter the podcast feed URL',
            default: 'https://anchor.fm/s/238d77c8/podcast/rss',
        },
        {
            type: 'input',
            name: 'channelId',
            message: 'Enter the discord channel Id',
            default: '1203413874183774290',
        },
    ] as any;

    const answers = await inquirer.prompt(questions);
    const podcast = await podcastHelpers.getPodcastFromUrl(answers.feedUrl || '');
    podcastDataStorage.addFeed(podcast, answers.channelId || '');
}

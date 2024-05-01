import exitHook from 'exit-hook';
import { Logger } from 'log4js';
import { Container } from './container.js';
import Bot from './services/bot.js';
import DiscordConnection from './services/discord/discord-connection.js';
import DiscordInteractionListener from './services/discord/discord-interaction-listener.js';
import PodcastDataStorage from './services/podcast/podcast-data-storage.js';
import PodcastHelpers from './services/podcast/podcast-helpers.js';

const processRestInterval = 10000;
const feedPageLength = 20;

try {
    const container = new Container();
    run(container);
} catch (error) {
    console.log('❌ Unable to run Application');
    console.log(error);
}

async function run(container: Container) {
    await container.register();
    const discordConnection = container.resolve<DiscordConnection>('discordConnection');
    const discordClient = await discordConnection.getClient();

    // Setup cleanup for when the application exits
    const exitHookFunction = container.resolve<typeof exitHook>('exitHook');
    exitHookFunction((signal) => {
        console.log(`Program Terminated: ${signal}`);
        discordClient.destroy();
        container.resolve<PodcastDataStorage>('podcastDataStorage').close();
    });

    // Log number of servers in use
    const serverCount: number = discordClient.guilds.cache.size;
    const logger = container.resolve<Logger>('logger');
    logger.info(`Discord Client Logged In on ${serverCount} Servers`);

    // Listen for discord interactions and respond
    const discordInteractionListener = container.resolve<DiscordInteractionListener>(
        'discordInteractionListener',
    );
    const bot = container.resolve<Bot>('bot');
    discordInteractionListener.startListeners(bot.receiveInteraction.bind(bot));

    // Open the database connection
    const data = container.resolve<PodcastDataStorage>('podcastDataStorage');
    const startTime = Date.now();
    let feedPage = 1;
    const getNewFeeds = async () => {
        // Kill the process if 24 hours have passed from starting
        if (Date.now() > startTime + 24 * 60 * 60000) {
            logger.info('24 Hour Reset');
            return process.exit();
        }

        // Get some feeds and reset or increment index as neccesary.
        const feedUrlList = data.getFeedUrlPage(feedPage, feedPageLength);
        if (feedUrlList.length === 0) {
            // Don't exit early here, nothing will happen because the list is empty.
            feedPage = 1;
        } else {
            feedPage++;
        }

        // Get the helpers setup
        const podcastHelpers = container.resolve<PodcastHelpers>('podcastHelpers');

        // Fetch podcast data for page of feeds
        const podcastsList = [];
        for (const feedUrl of feedUrlList) {
            try {
                const podcast = await podcastHelpers.getPodcastFromUrl(feedUrl);
                podcastsList.push(podcast);
            } catch (error) {}
        }

        // Post most recent podcast
        for (const podcast of podcastsList) {
            // Send some episode information if there is a new episode
            if (podcastHelpers.mostRecentPodcastEpisodeIsNew(podcast)) {
                await bot.sendMostRecentPodcastEpisode(podcast);
            }
        }

        // Kick off the process again
        setTimeout(getNewFeeds, processRestInterval);
    };
    setTimeout(getNewFeeds, processRestInterval);
}

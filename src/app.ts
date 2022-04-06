#!/usr/bin/env node

import { Client as DiscordClient, CommandInteraction } from 'discord.js';
import { Logger } from 'log4js';
import onExit from 'signal-exit';
import { Container } from './container.js';
import { Podcast } from './models/podcast.js';
import { Bot } from './services/bot.js';
import { DiscordConnection } from './services/discord/discord-connection.js';
import { DiscordInteractionListener } from './services/discord/discord-interaction-listener';
import { PodcastDataStorage } from './services/podcast/podcast-data-storage.js';
import { PodcastHelpers } from './services/podcast/podcast-helpers.js';
import { PodcastRssProcessor } from './services/podcast/podcast-rss-processor.js';

// Initialize the container
const container: Container = new Container(process.argv[2] || '');
container.register().then(() => {
    // Catch a couple special use cases for now
    switch (process.argv[3]) {
        case 'reset-guids':
            container.resolve<PodcastDataStorage>('podcastDataStorage').resetPostedData();
            break;
    }

    // Get a Discord connection to use in the primary loop
    container
        .resolve<DiscordConnection>('discordConnection')
        .getConnectedClient()
        .then((discordClient: DiscordClient) => {
            // Log a message on successful connection
            const serverCount: Number = discordClient.guilds.cache.size;
            const logger = container.resolve<Logger>('logger');
            logger.debug(`Discord Client Logged In on ${serverCount} Servers`);

            // Listen for discord interactions and respond
            const bot = container.resolve<Bot>('bot');
            container
                .resolve<DiscordInteractionListener>('discordInteractionListener')
                .onInteraction((commandInteraction: CommandInteraction) => {
                    bot.actOnCommandInteraction(commandInteraction);
                });

            // Keeps track of if an active diary entry thread is running
            let threadRunning: boolean = false;
            const processRestInterval: number = 60000; // Give it up to 60 seconds to rest

            // Open the database connection
            const data = container.resolve<PodcastDataStorage>('podcastDataStorage');
            const startTime = Date.now();
            const interval = setInterval(() => {
                // Kill the process if 24 hours have passed regardless of thread status
                if (Date.now() > startTime + 24 * 60 * 60000) {
                    logger.info('24 Hour Reset');
                    return process.exit();
                }

                // Skip the loop if the thread is still running
                if (threadRunning) {
                    return;
                }

                // Indicate that processing has started
                threadRunning = true;

                // TODO: Make this scale properly, this currently gets all the feeds
                const feeds = data.getPostedFeeds();
                const processor = container.resolve<PodcastRssProcessor>('podcastRssProcessor');
                const helpers = container.resolve<PodcastHelpers>('podcastHelpers');
                const bot = container.resolve<Bot>('bot');

                // Create list of feed processing promises with noop failures
                const entryPromiseList = feeds
                    .map((feedUrl) => processor.process(feedUrl, 1))
                    .map((p) => p.catch(() => null));

                Promise.all(entryPromiseList)
                    .then((results) => {
                        // Filter out invalid results and podcasts without new episodes
                        const podcasts = results
                            .filter((result): result is Podcast => !!result)
                            .filter((podcast) => !helpers.podcastHasLatestEpisode(podcast));

                        // Create list of announcement promises with noop failures
                        const announcePromiseList = podcasts
                            .map((podcast) => bot.sendNewEpisodeAnnouncement(podcast))
                            .filter((result): result is Promise<void> => !!result);

                        return Promise.all(announcePromiseList);
                    })
                    .then(() => {
                        threadRunning = false;
                    });
            }, processRestInterval);

            // Clean up when process is told to end
            onExit((code: any, signal: any) => {
                clearInterval(interval);
                discordClient.destroy();
                container.resolve<PodcastDataStorage>('podcastDataStorage').close();
                logger.debug(`Program Terminated ${code}:${signal}`);
            });
        });
});

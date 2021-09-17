#!/usr/bin/env node

import { Client as DiscordClient } from 'discord.js';
import { Logger } from 'log4js';
import onExit from 'signal-exit';
import { Container } from './container.js';
import { IncomingMessage } from './models/incoming-message.js';
import { Podcast } from './models/podcast.js';
import { Bot } from './services/bot.js';
import { DiscordConnection } from './services/discord/discord-connection.js';
import { DiscordDataStorage } from './services/discord/discord-data-storage.js';
import { DiscordMessageListener } from './services/discord/discord-message-listener.js';
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

            // Get a Discord message listener to capture input
            const bot = container.resolve<Bot>('bot');
            container
                .resolve<DiscordMessageListener>('discordMessageListener')
                .onMessage((incomingMessage: IncomingMessage) => {
                    bot.actOnUserMessage(incomingMessage);
                });

            // Keeps track of if an active diary entry thread is running
            let threadRunning: boolean = false;
            const processRestInterval: number = 60000; // Give it up to 60 seconds to rest

            // Open the database connection
            const data = container.resolve<PodcastDataStorage>('podcastDataStorage');
            const startTime = Date.now();
            const interval = setInterval(() => {
                if (threadRunning) return;

                // Kill the process if 12 hours have passed
                if (Date.now() > startTime + 12 * 60 * 60000) {
                    container.resolve<Logger>('logger').info('12 Hour Reset');
                    return process.exit();
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
                container.resolve<DiscordDataStorage>('discordDataStorage').close();
                container.resolve<PodcastDataStorage>('podcastDataStorage').close();
                logger.debug(`Program Terminated ${code}:${signal}`);
            });
        });
});

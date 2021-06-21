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
const container: Container = new Container();
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
            let interval: NodeJS.Timeout;
            const processRestInterval: number = 60000; // Give it up to 60 seconds to rest

            // Open the database connection
            const data = container.resolve<PodcastDataStorage>('podcastDataStorage');
            interval = setInterval(() => {
                if (threadRunning) return;

                const feeds = data.getPostedFeeds();
                const processor = container.resolve<PodcastRssProcessor>('podcastRssProcessor');
                const bot = container.resolve<Bot>('bot');

                let feedCount = 1; // Adjust for length index-zero
                feeds.forEach((feedUrl: string) => {
                    // If we have entered the forEach loop stop later processes from doing the same
                    threadRunning = true;

                    processor
                        .process(feedUrl, 1)
                        .then((podcast: Podcast) => {
                            // Exit early if the podcast is already latest
                            const helpers = container.resolve<PodcastHelpers>('podcastHelpers');
                            if (helpers.podcastHasLatestEpisode(podcast)) {
                                return;
                            }
                            // Write podcast to a channel list
                            bot.sendNewEpisodeAnnouncement(podcast);
                        })
                        .catch((error: string) => {
                            logger.error(`Problem Processing Feed [${error}]`);
                        })
                        .finally(() => {
                            // Allow the thread to start again after all feeds have completed
                            // Incrementing count in the finally means no matter what it increments
                            if (feedCount++ === feeds.length) {
                                threadRunning = false;
                            }
                        });
                });
            }, processRestInterval);

            // Clean up when process is told to end
            onExit((code: any, signal: any) => {
                discordClient.destroy();
                container.resolve<DiscordDataStorage>('discordDataStorage').close();
                container.resolve<PodcastDataStorage>('podcastDataStorage').close();
                logger.debug(`Program Terminated ${code}:${signal}`);
            });
        });
});

#!/usr/bin/env node

import { Client as DiscordClient } from 'discord.js';
import { Logger } from 'log4js';
import onExit from 'signal-exit';
import { Container } from './container.js';
import { Message } from './models/message.js';
import { Podcast } from './models/podcast.js';
import { Bot } from './services/bot.js';
import { DiscordConnection } from './services/discord/discord-connection';
import { DiscordDataStorage } from './services/discord/discord-data-storage.js';
import { DiscordMessageListener } from './services/discord/discord-message-listener';
import { PodcastDataStorage } from './services/podcast/podcast-data-storage';
import { PodcastProcessor } from './services/podcast/podcast-processor';

// Initialize the container
const container: Container = new Container();
container.register().then(() => {
    // Setup this Data Storage, It can happen async probably?
    container.resolve<DiscordDataStorage>('discordDataStorage').setup();

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
                .onMessage((message: Message) => {
                    bot.actOnUserMessage(message);
                });

            // Keeps track of if an active diary entry thread is running
            let threadRunning: boolean = false;
            let interval: NodeJS.Timeout;
            const processRestInterval: number = 60000; // Give it up to 60 seconds to rest

            // Open the database connection
            const data = container.resolve<PodcastDataStorage>('podcastDataStorage');
            data.setup().then(() => {
                interval = setInterval(() => {
                    if (threadRunning) return;

                    const feeds = data.getPostedFeeds();
                    const used = process.memoryUsage().heapUsed / 1024 / 1024;

                    logger.debug(`Running on ${feeds.length} Feeds [${used} MB]`);
                    // TODO: If there are zero feeds this doesn't work at all.

                    const processor = container.resolve<PodcastProcessor>('podcastProcessor');
                    const bot = container.resolve<Bot>('bot');

                    let feedCount = 1; // Adjust for length index-zero
                    feeds.forEach((feedUrl: string, index: number) => {
                        processor
                            .process(feedUrl)
                            .then((podcast: Podcast) => {
                                // Exit early if the podcast is already latest
                                if (bot.podcastIsLatest(podcast)) {
                                    return;
                                }
                                // Write podcast to a channel list
                                bot.writePodcastAnnouncement(podcast);
                            })
                            .catch((error: string) => {
                                logger.error(`Problem Processing Feeds [${error}]`);
                            })
                            .finally(() => {
                                // Allow the thread to start again
                                if (feedCount++ === feeds.length) {
                                    threadRunning = false;
                                }
                            });
                    });

                    threadRunning = true;
                }, processRestInterval);
            });

            // Clean up when process is told to end
            onExit((code: any, signal: any) => {
                discordClient.destroy();
                data.close();
                logger.debug(`Program Terminated ${code}:${signal}`);
            });
        });
});

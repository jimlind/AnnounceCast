#!/usr/bin/env node

import { Client as DiscordClient } from 'discord.js';
import { Logger } from 'log4js';
import onExit from 'signal-exit';
import { Container } from './container.js';
import { IncomingMessage } from './models/incoming-message.js';
import { PodcastEpisode } from './models/podcast-episode.js';
import { Bot } from './services/bot.js';
import { DiscordConnection } from './services/discord/discord-connection';
import { DiscordDataStorage } from './services/discord/discord-data-storage.js';
import { DiscordMessageListener } from './services/discord/discord-message-listener';
import { PodcastDataStorage } from './services/podcast/podcast-data-storage';
import { PodcastProcessor } from './services/podcast/podcast-processor';

// Initialize the container
const container: Container = new Container();
container.register().then(() => {
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
                const used = process.memoryUsage().heapUsed / 1024 / 1024;

                logger.debug(`Running on ${feeds.length} Feeds [${used} MB]`);
                // TODO: If there are zero feeds this doesn't work at all.

                const processor = container.resolve<PodcastProcessor>('podcastProcessor');
                const bot = container.resolve<Bot>('bot');

                let feedCount = 1; // Adjust for length index-zero
                feeds.forEach((feedUrl: string, index: number) => {
                    processor
                        .process(feedUrl)
                        .then((podcastEpisode: PodcastEpisode) => {
                            // Exit early if the podcast is already latest
                            if (bot.podcastIsLatest(podcastEpisode)) {
                                return;
                            }
                            // Write podcast to a channel list
                            bot.writePodcastAnnouncement(podcastEpisode);
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

            // Clean up when process is told to end
            onExit((code: any, signal: any) => {
                discordClient.destroy();
                data.close();
                container.resolve<DiscordDataStorage>('discordDataStorage').close();
                logger.debug(`Program Terminated ${code}:${signal}`);
            });
        });
});

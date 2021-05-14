#!/usr/bin/env node

import { Client as DiscordClient } from 'discord.js';
import onExit from 'signal-exit';
import { Container } from './container.js';
import { Podcast } from './models/podcast.js';
import { DiscordConnection } from './services/discord/discord-connection';
import { DiscordMessageFactory } from './services/discord/discord-message-factory';
import { DiscordMessageSender } from './services/discord/discord-message-sender';
import { PodcastDataStorage } from './services/podcast/podcast-data-storage';
import { PodcastProcessor } from './services/podcast/podcast-processor';

// Initialize the container
const container: Container = new Container();
container.register().then(() => {
    // Activate the Discord connection
    container
        .resolve<DiscordConnection>('discordConnection')
        .getConnectedClient()
        .then((discordClient: DiscordClient) => {
            // Log a message on successful connection
            const serverCount: Number = discordClient.guilds.cache.size;
            console.log(`Discord Client Logged In on ${serverCount} Servers`);

            // Open the database connection
            const data = container.resolve<PodcastDataStorage>('podcastDataStorage');

            // Keeps track of if an active diary entry thread is running
            let threadRunning: boolean = false;
            let interval: NodeJS.Timeout;
            const processRestInterval: number = 1; //60000; // Give it up to 60 seconds to rest

            data.setup().then(() => {
                data.getPostedData().then((postedData: any) => {
                    interval = setInterval(() => {
                        if (threadRunning) return;

                        const feeds = [
                            'https://feeds.simplecast.com/DPfrjtYE', // Film Hags
                            'https://anchor.fm/s/184b0a38/podcast/rss', // Bat & Spider
                            'https://anchor.fm/s/23694498/podcast/rss', // Will Run For...
                            'https://anchor.fm/s/3a0acd20/podcast/rss', // Cinenauts
                            'https://anchor.fm/s/12d1fabc/podcast/rss', // 70mm
                            'https://anchor.fm/s/238d77c8/podcast/rss', // Dune Pod
                            'https://anchor.fm/s/3ae14da0/podcast/rss', // Lost Light
                        ];

                        const channels = [
                            '799785154032959528', // Bot Dev Channel
                            //'842188710393151519', // TAPEDECK Feed Channel
                        ];

                        const processor = container.resolve<PodcastProcessor>('podcastProcessor');

                        let feedCount = 1; // Adjust for length index-zero
                        feeds.forEach((feedUrl: string, index: number) => {
                            setTimeout(() => {
                                processor.process(feedUrl).then((podcast: Podcast) => {
                                    // Prodcast fetching and process completed (the hard part)
                                    // Allow the thread to start again
                                    if (feedCount++ === feeds.length) {
                                        threadRunning = false;
                                    }

                                    // Exit early if the podcast isn't latest
                                    if (podcast.episodeGuid === postedData[podcast.showFeed]) {
                                        return;
                                    }

                                    const message = container
                                        .resolve<DiscordMessageFactory>('discordMessageFactory')
                                        .build(podcast);

                                    channels.forEach((channelId: string) => {
                                        container
                                            .resolve<DiscordMessageSender>('discordMessageSender')
                                            .send(channelId, message)
                                            .then(() => {
                                                postedData[podcast.showFeed] = podcast.episodeGuid;
                                                data.updatePostedData(
                                                    podcast.showFeed,
                                                    podcast.episodeGuid,
                                                );
                                            });
                                    });
                                });
                            }, 1000 * index);
                        });

                        threadRunning = true;
                    }, processRestInterval);
                });
            });

            // Clean up when process is told to end
            onExit((code: any, signal: any) => {
                discordClient.destroy();
                data.close();
                console.log(`Program Terminated ${code}:${signal}`);
            });
        });
});

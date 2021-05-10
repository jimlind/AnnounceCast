#!/usr/bin/env node

import { Client as DiscordClient } from 'discord.js';
import * as onExit from 'signal-exit';
import { Container } from './container';
import { Podcast } from './models/podcast';
import { DiscordConnection } from './services/discord/discord-connection';
import { DiscordMessageFactory } from './services/discord/discord-message-factory';
import { DiscordMessageSender } from './services/discord/discord-message-sender';
import { PodcastProcessor } from './services/podcast/podcast-processor';

// Initialize the container
const container: Container = new Container();

// Activate the Discord connection
container
    .resolve<DiscordConnection>('discordConnection')
    .getConnectedClient()
    .then((discordClient: DiscordClient) => {
        // Log a message on successful connection
        const serverCount: Number = discordClient.guilds.cache.size;
        console.log(`Discord Client Logged In on ${serverCount} Servers`);

        // Keeps track of if an active diary entry thread is running
        let threadRunning: boolean = false;
        let interval: NodeJS.Timeout;
        const processRestInterval: number = 1000; // Give it 1 second to rest

        interval = setInterval(() => {
            if (threadRunning) return;

            const feeds = [
                'https://feeds.simplecast.com/DPfrjtYE', // Hags
                'https://anchor.fm/s/184b0a38/podcast/rss', // BANZ
                'https://anchor.fm/s/23694498/podcast/rss', // WR4
                'https://anchor.fm/s/3a0acd20/podcast/rss', // Nauts
                'https://anchor.fm/s/12d1fabc/podcast/rss', // 70mm
                'https://anchor.fm/s/238d77c8/podcast/rss', // Dune
                'https://anchor.fm/s/3ae14da0/podcast/rss', // Lost
            ];
            const processor = container.resolve<PodcastProcessor>('podcastProcessor');

            feeds.forEach((feedUrl: string) => {
                processor.process(feedUrl).then((podcast: Podcast) => {
                    const message = container
                        .resolve<DiscordMessageFactory>('discordMessageFactory')
                        .build(podcast);

                    container
                        .resolve<DiscordMessageSender>('discordMessageSender')
                        .send('812883182390607933', message)
                        .then(() => {
                            console.log('Message Sent');
                        });
                });
            });

            threadRunning = true;
        }, processRestInterval);

        // Clean up when process is told to end
        onExit((code, signal) => {
            discordClient.destroy();
            console.log(`Program Terminated ${code}:${signal}`);
        });
    });

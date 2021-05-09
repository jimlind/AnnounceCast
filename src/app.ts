#!/usr/bin/env node

import { Client as DiscordClient } from 'discord.js';
import * as onExit from 'signal-exit';
import { Container } from './container';
import { Podcast } from './models/podcast';
import { DiscordConnection } from './services/discord/discord-connection';
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

            const processor = container.resolve<PodcastProcessor>('podcastProcessor');
            processor.process().then((podcast: Podcast) => {
                console.log(podcast);
            });

            threadRunning = true;
        }, processRestInterval);

        // Clean up when process is told to end
        onExit((code, signal) => {
            discordClient.destroy();
            console.log(`Program Terminated ${code}:${signal}`);
        });
    });

#!/usr/bin/env node

import * as onExit from 'signal-exit';
import { Container } from './container';
import { DiscordConnection } from './services/discord/discord-connection';
import { Client as DiscordClient } from 'discord.js';

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

        // Clean up when process is told to end
        onExit((code, signal) => {
            discordClient.destroy();
            console.log(`Program Terminated ${code}:${signal}`);
        });
    });

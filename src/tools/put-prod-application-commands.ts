#!/usr/bin/env node

import { Container } from '../container.js';
import { Config } from '../models/config';
import { DiscordConnection } from '../services/discord/discord-connection.js';
import commandData from './data/command.json';

const container: Container = new Container('prod');
container.register().then(() => {
    const discordRest = container.resolve<typeof import('@discordjs/rest').REST>('discordRest');
    const config = container.resolve<Config>('config');
    const rest = new discordRest({ version: '9' }).setToken(config.discordBotToken);
    const commandRoute = container
        .resolve<typeof import('discord-api-types/v9').Routes>('discordRoutes')
        .applicationCommands(config.discordClientId);

    rest.put(commandRoute, { body: commandData })
        .then((commandList) => {
            if (Array.isArray(commandList)) {
                console.log(`✅  Set ${commandList.length} prod Application Commands`);
            } else {
                console.log('❌ Set dev Application Commands but incorrect return type');
            }
        })
        .catch(() => {
            console.log('❌ Unable to set dev Application Commands');
        })
        .finally(() => {
            // This is a bit silly but registering the container created the client so we need to destroy it
            container.resolve<DiscordConnection>('discordConnection').getClient().destroy();
        });
});

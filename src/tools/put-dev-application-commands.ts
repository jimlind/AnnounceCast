#!/usr/bin/env node

import { readFileSync } from 'fs';
import { Container } from '../container.js';
import { Config } from '../models/config';
import { DiscordConnection } from '../services/discord/discord-connection.js';

const guildId = '795053930283139073';
const container: Container = new Container('dev');
container.register().then(() => {
    const discordRest = container.resolve<typeof import('@discordjs/rest').REST>('discordRest');
    const config = container.resolve<Config>('config');
    const rest = new discordRest({ version: '9' }).setToken(config.discordBotToken);
    const commandRoute = container
        .resolve<typeof import('discord-api-types/v9').Routes>('discordRoutes')
        .applicationGuildCommands(config.discordClientId, guildId);

    const commandBuffer = readFileSync(new URL('./data/command.json', import.meta.url));
    const commandData = JSON.parse(commandBuffer.toString());

    rest.put(commandRoute, { body: commandData })
        .then((commandList) => {
            if (Array.isArray(commandList)) {
                console.log(`✅  Set ${commandList.length} dev Application Commands`);
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

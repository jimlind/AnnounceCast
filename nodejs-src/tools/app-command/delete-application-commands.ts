/**
 * Run this command in the terminal for prod or dev like this:
 * >>> NODE_ENV=production node --import ./register.mjs ./src/tools/app-command/delete-application-commands.ts
 * >>> NODE_ENV=development node --import ./register.mjs ./src/tools/app-command/delete-application-commands.ts
 */

import { Container } from '../../container.js';

try {
    const container = new Container();
    run(container);
} catch (error) {
    console.log('❌ Unable to set Application Commands');
    console.log(error);
}

async function run(container: Container) {
    await container.register();

    const discordRest = container.resolve<import('discord.js').REST>('discordRest');
    const discordCommandRoutes = container.resolve<`/${string}`>('discordCommandRoutes');
    const commandList = await discordRest.put(discordCommandRoutes, { body: [] });

    if (Array.isArray(commandList)) {
        console.log(`✅  Set ${commandList.length} Application Commands`);
    } else {
        console.log('❌ Set Application Commands but incorrect return type');
    }
}

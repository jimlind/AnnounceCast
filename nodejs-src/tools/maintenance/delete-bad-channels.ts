/**
 * Run this command in the terminal against the local database like this:
 * >>> NODE_ENV=production node --import ./register.mjs ./src/tools/maintenance/delete-bad-channels.ts
 * >>> NODE_ENV=development node --import ./register.mjs ./src/tools/maintenance/delete-bad-channels.ts
 */

import { confirm } from '@inquirer/prompts';
import bettersqlite3 from 'better-sqlite3';
import { BaseGuildTextChannel, PermissionsBitField, ThreadChannel } from 'discord.js';
import fs from 'fs';
import { Container } from '../../container.js';
import DiscordConnection from '../../services/discord/discord-connection.js';

const outputFolder = './src/tools/maintenance/output';
const outputFile = `${outputFolder}/deleted-bad-channels-${Date.now()}.csv`;

try {
    fs.mkdirSync(outputFolder);
} catch (e) {
    // Do nothing output folder exists
}

try {
    console.log('--------------------');
    const container = new Container();
    await run(container);
    console.log('✅ Command completed');
} catch (error) {
    console.log('❌ Unable to run command');
    console.log(error);
}
process.exit();

async function run(container: Container) {
    await container.register();
    const betterSqlite3 = container.resolve<typeof bettersqlite3>('betterSqlite3');
    const discordConnection = container.resolve<DiscordConnection>('discordConnection');
    const discordClient = await discordConnection.getClient();

    const database = betterSqlite3('./db/podcasts.db');
    let channelIdList = database
        .prepare('SELECT channel_id FROM channels')
        .pluck()
        .all()
        .map(String);
    // Ensure only unique values by using the Set
    channelIdList = Array.from(new Set(channelIdList));

    let badChannelIdList = [];
    for (let index = 0; index < 10; index++) {
        process.stdout.write(`${channelIdList.length} channels to check - `);
        badChannelIdList = [];

        for (const channelId of channelIdList) {
            try {
                const channel = await discordClient.channels.fetch(channelId);

                // This covers all the current text channels (announcments, threads, etc)
                if (
                    !(channel instanceof BaseGuildTextChannel) &&
                    !(channel instanceof ThreadChannel)
                ) {
                    throw new Error('Channel is not a textual channel');
                }

                const botPermissions = channel.permissionsFor(discordClient.user || '');
                // Check that the bot has some ability to send messages
                if (
                    !botPermissions?.has(PermissionsBitField.Flags.SendMessages) &&
                    !botPermissions?.has(PermissionsBitField.Flags.SendMessagesInThreads)
                ) {
                    throw new Error('Channel does not have sending permissions');
                }

                // Check that the bot has can view the channel and embed links
                if (
                    !botPermissions?.has([
                        PermissionsBitField.Flags.ViewChannel,
                        PermissionsBitField.Flags.EmbedLinks,
                    ])
                ) {
                    throw new Error('Channel does not have view or embed');
                }

                process.stdout.write('.');
            } catch (error) {
                badChannelIdList.push({ channelId, error });

                process.stdout.write('✗');
            }
        }
        process.stdout.write('\n');
        channelIdList = badChannelIdList.map((x) => x.channelId);
    }

    await purgeChannels(badChannelIdList, database);
    database.close();
}

async function purgeChannels(
    channelIdList: { channelId: string; error: unknown }[],
    database: bettersqlite3.Database,
) {
    // Prompt for actual deletes
    const live = await confirm({ message: 'Perform actual deletes?' });

    for (const channelError of channelIdList) {
        const channelId = channelError.channelId;
        const query =
            'SELECT url, channel_id FROM feeds f JOIN channels c ON f.id = c.feed_id WHERE channel_id = ?';
        const channelList = database.prepare(query).all(channelId).map(Object);

        for (const channel of channelList) {
            const url = channel.url || '';
            const cid = channel.channel_id || '';
            fs.appendFileSync(outputFile, `"${url}","${cid}"\n`, 'utf-8');

            const message = `Deleting channel ${cid} with podcast ${url} because "${channelError.error}"\n`;
            process.stdout.write(message);
        }

        let channelsResult;
        // Do the actual deletes
        if (live) {
            channelsResult = database
                .prepare('DELETE FROM channels WHERE channel_id = ?')
                .run(channelId);
        }
        const changes = channelsResult?.changes || 0;
        console.log(`🚮 ${changes}x changes for channel ${channelId}`);
    }
}

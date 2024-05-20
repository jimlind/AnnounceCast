/**
 * Run this command in the terminal against the local database like this:
 * >>> NODE_ENV=production node --loader ts-node/esm src/tools/maintenance/delete-bad-channels.ts
 * >>> NODE_ENV=development node --loader ts-node/esm src/tools/maintenance/delete-bad-channels.ts
 */

import bettersqlite3 from 'better-sqlite3';
import { PermissionsBitField, TextChannel } from 'discord.js';
import fs from 'fs';
import { Container } from '../../container.js';
import DiscordConnection from '../../services/discord/discord-connection.js';

const outputFile = `./src/tools/maintenance/output/deleted-bad-channels-${Date.now()}.csv`;
const dryRun = true;

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

    for (let index = 0; index < 10; index++) {
        process.stdout.write(`${channelIdList.length} channels to check - `);
        const badChannelIdList = [];

        for (const channelId of channelIdList) {
            try {
                const channel = await discordClient.channels.fetch(channelId);
                if (!(channel instanceof TextChannel)) {
                    throw new Error('Channel is not a text channel');
                }

                const botPermissions = channel?.permissionsFor(discordClient.user || '');
                const hasPermissions = botPermissions?.has([
                    PermissionsBitField.Flags.SendMessages,
                    PermissionsBitField.Flags.EmbedLinks,
                ]);
                if (!hasPermissions) {
                    throw new Error('Channel does not have permissions');
                }
                process.stdout.write('.');
            } catch (error) {
                process.stdout.write('✗');
                badChannelIdList.push(channelId);
            }
        }
        process.stdout.write('\n');
        channelIdList = badChannelIdList;
    }

    purgeChannels(channelIdList, database);
    database.close();
}

async function purgeChannels(channelIdList: string[], database: bettersqlite3.Database) {
    for (const channelId of channelIdList) {
        const query =
            'SELECT url, channel_id FROM feeds f JOIN channels c ON f.id = c.feed_id WHERE channel_id = ?';
        const channelList = database.prepare(query).all(channelId).map(Object);

        for (const channel of channelList) {
            const url = channel.url || '';
            const cid = channel.channel_id || '';
            fs.appendFileSync(outputFile, `"${url}","${cid}"\n`, 'utf-8');
        }

        let channelsResult;
        if (!dryRun) {
            channelsResult = database
                .prepare('DELETE FROM channels WHERE channel_id = ?')
                .run(channelId);
        }
        const changes = channelsResult?.changes || 0;
        console.log(`🚮 ${changes}x changes for channel ${channelId}`);
    }
}

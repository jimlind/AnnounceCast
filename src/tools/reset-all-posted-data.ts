import { Container } from '../container.js';
import bettersqlite3 from 'better-sqlite3';
import { DiscordConnection } from '../services/discord/discord-connection.js';

const container: Container = new Container();
container.register().then(() => {
    const bs = container.resolve<typeof bettersqlite3>('betterSqlite3');
    const db = bs('./db/podcasts.db');
    db.prepare('DELETE FROM posted').run();

    // This is a bit silly but registering the container created the client so we need to destroy it
    container.resolve<DiscordConnection>('discordConnection').getClient().destroy();

    console.log('✅  Complete');
});

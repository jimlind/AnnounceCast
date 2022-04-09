import { Container } from '../container.js';
import bettersqlite3 from 'better-sqlite3';

const container: Container = new Container('dev');
container.register().then(() => {
    const bs = container.resolve<typeof bettersqlite3>('betterSqlite3');
    const db = bs('./db/podcasts.db');

    const rows = db.prepare('SELECT feed_id, guid FROM posted').all();
    console.table(rows);
});

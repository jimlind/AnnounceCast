#!/usr/bin/env ts-node
/**
 * Run this command in the terminal for against the local database like this:
 * >>> node --loader ts-node/esm src/tools/maintenance/print-all-feeds.ts
 */

import bettersqlite3 from 'better-sqlite3';
import { Container } from '../../container.js';

try {
    const container = new Container();
    await run(container);
} catch (error) {
    console.log('❌ Unable to run command');
    console.log(error);
}

async function run(container: Container) {
    const betterSqlite3 = container.resolve<typeof bettersqlite3>('betterSqlite3');
    const database = betterSqlite3('./db/podcasts.db');

    const rows = database.prepare('SELECT title, url FROM feeds').all();
    console.table(rows);

    database.close();
}

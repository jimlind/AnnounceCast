#!/usr/bin/env ts-node
/**
 * Run this command in the terminal against the local database like this:
 * >>> node --loader ts-node/esm src/tools/maintenance/delete-posted.ts
 */

import bettersqlite3 from 'better-sqlite3';
import { Container } from '../../container.js';

try {
    console.log('--------------------');
    const container = new Container();
    await run(container);
    console.log('✅ Command completed');
} catch (error) {
    console.log('❌ Unable to run command');
    console.log(error);
}

async function run(container: Container) {
    await container.register();
    const betterSqlite3 = container.resolve<typeof bettersqlite3>('betterSqlite3');

    const database = betterSqlite3('./db/podcasts.db');
    const deleteResults = database.prepare('DELETE FROM posted').run();
    database.close();

    console.log(`🚮 ${deleteResults.changes}x changes for posted table`);
}

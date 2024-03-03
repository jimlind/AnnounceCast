#!/usr/bin/env ts-node
/**
 * Run this command in the terminal against the local database like this:
 * >>> node --loader ts-node/esm src/tools/maintenance/print-all-posted.ts
 */

import bettersqlite3 from 'better-sqlite3';
import fs from 'fs';
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

    const rows = database.prepare('SELECT feed_id, guid FROM posted').all();
    console.table(rows);
    writeToFile(rows);

    database.close();
}

function writeToFile(rows: any[]) {
    const outputFile = './src/tools/maintenance/output/all-posted.csv';
    const outputData = ['"feed_id","guid"'];

    for (const row of rows) {
        const object = Object(row);
        const feedId = String(object.feed_id).replace('"', '""');
        const guidList = String(object.guid).replace('"', '""');

        outputData.push(`"${feedId}","${guidList}"`);
    }

    fs.writeFileSync(outputFile, outputData.join('\n'), 'utf-8');
}

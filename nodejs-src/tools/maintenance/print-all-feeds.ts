/**
 * Run this command in the terminal against the local database like this:
 * >>> node --import ./register.mjs ./src/tools/maintenance/print-all-feeds.ts
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

    const rows = database.prepare('SELECT title, url FROM feeds').all();
    console.table(rows);
    writeToFile(rows);

    database.close();
}

function writeToFile(rows: unknown[]) {
    const outputFile = './src/tools/maintenance/output/all-feeds.csv';
    const outputData = ['"title","url"'];

    for (const row of rows) {
        const object = Object(row);
        const title = String(object.title).replace('"', '""');
        const url = String(object.url).replace('"', '""');

        outputData.push(`"${title}","${url}"`);
    }

    fs.writeFileSync(outputFile, outputData.join('\n'), 'utf-8');
}

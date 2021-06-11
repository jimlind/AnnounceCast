import { RESOLVER } from 'awilix';
import sqlite3 from 'sqlite3';

type Dictionary = {
    [key: string]: any;
};

export class DiscordDataStorage {
    static [RESOLVER] = {};

    db: sqlite3.Database;
    prefixCache: Dictionary;

    constructor(sqlite3: sqlite3.sqlite3) {
        this.db = new sqlite3.Database('./db/servers.db');
        this.prefixCache = {};
    }

    setup(): Promise<void> {
        return new Promise((resolve) => {
            function createPrefixes(db: sqlite3.Database, callback: Function) {
                db.run(
                    'CREATE TABLE IF NOT EXISTS prefixes (guild_id TEXT PRIMARY KEY, prefix TEXT)',
                    callback,
                );
            }

            createPrefixes(this.db, () => {
                this.cachePrefixesDataLocally().then(() => {
                    return resolve();
                });
            });
        });
    }

    getPrefix(guildId: string): string {
        return this.prefixCache[guildId] || '!';
    }

    setPrefix(guildId: string, prefix: string) {
        this.prefixCache[guildId] = prefix;
        this.db.run('REPLACE INTO prefixes (guild_id, prefix) VALUES (?, ?)', [guildId, prefix]);
    }

    cachePrefixesDataLocally(): Promise<void> {
        return new Promise((resolve) => {
            this.db.all('SELECT guild_id, prefix FROM prefixes', (err, rows) => {
                const reducedRows = rows.reduce((accumulator, current) => {
                    return { ...accumulator, [current.guild_id]: current.prefix };
                }, {});
                // Set the local cache
                this.prefixCache = reducedRows;
                // Resolve promise
                return resolve();
            });
        });
    }

    close() {
        this.db.close();
    }
}

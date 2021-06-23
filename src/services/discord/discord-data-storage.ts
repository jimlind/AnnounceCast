import { RESOLVER } from 'awilix';
import bettersqlite3 from 'better-sqlite3';
import { CacheDictionary } from '../../models/cache-dictionary.js';

export class DiscordDataStorage {
    static [RESOLVER] = {};

    db: bettersqlite3.Database;
    prefixCache: CacheDictionary;

    constructor(betterSqlite3: typeof bettersqlite3) {
        this.db = betterSqlite3('./db/servers.db');
        this.prefixCache = new CacheDictionary('!');

        this.setup();
    }

    setup() {
        this.db.exec(
            'CREATE TABLE IF NOT EXISTS prefixes (guild_id TEXT PRIMARY KEY, prefix TEXT)',
        );
        this.cachePrefixesDataLocally();
    }

    cachePrefixesDataLocally() {
        const allRows = this.db.prepare('SELECT guild_id, prefix FROM prefixes').all();
        for (var x = 0; x < allRows.length; x++) {
            const row = allRows[x];
            this.prefixCache.set(row.guild_id || '', row.prefix || '');
        }
    }

    getPrefix(guildId: string): string {
        return this.prefixCache.get(guildId);
    }

    setPrefix(guildId: string, prefix: string) {
        this.prefixCache.set(guildId, prefix);
        this.db
            .prepare('REPLACE INTO prefixes (guild_id, prefix) VALUES (?, ?)')
            .run(guildId, prefix);
    }

    close() {
        this.db.close();
    }
}

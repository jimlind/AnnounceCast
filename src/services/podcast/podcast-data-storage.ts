import { RESOLVER } from 'awilix';
import sqlite3 from 'sqlite3';

type Dictionary = {
    [key: string]: any;
};

export class PodcastDataStorage {
    static [RESOLVER] = {};

    db: sqlite3.Database;
    postedCache: Dictionary;

    constructor(sqlite3: sqlite3.sqlite3) {
        this.db = new sqlite3.Database('./db/podcasts.db');
        this.postedCache = {};
    }

    setup(): Promise<void> {
        return new Promise((resolve) => {
            this.db.run(
                'CREATE TABLE IF NOT EXISTS posted (url TEXT PRIMARY KEY, guid TEXT)',
                () => {
                    resolve();
                },
            );
        });
    }

    getPostedData(): Promise<Array<Object>> {
        return new Promise((resolve) => {
            this.db.all('SELECT * FROM posted', (err, rows) => {
                const reducedRows = rows.reduce((accumulator, current) => {
                    return { ...accumulator, [current.url]: current.guid };
                }, {});
                // Set the local cache
                this.postedCache = reducedRows;
                // Return complete data
                resolve(reducedRows);
            });
        });
    }

    updatePostedData(url: string, guid: string) {
        this.db.serialize(() => {
            var stmt = this.db.prepare('REPLACE INTO posted (url, guid) VALUES (?, ?)');
            stmt.run(url, guid);
            stmt.finalize();
            // Update the local cache
            this.postedCache[url] = guid;
        });
    }

    getPostedFromUrl(url: string): string {
        return this.postedCache[url] || '';
    }

    close() {
        this.db.close();
    }
}

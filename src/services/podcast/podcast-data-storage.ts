import { RESOLVER } from 'awilix';
import sqlite3 from 'sqlite3';

export class PodcastDataStorage {
    static [RESOLVER] = {};

    db: sqlite3.Database;

    constructor(sqlite3: sqlite3.sqlite3) {
        this.db = new sqlite3.Database('./db/podcasts.db');
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
                console.log(err);

                const reducedRows = rows.reduce((accumulator, current) => {
                    return { ...accumulator, [current.url]: current.guid };
                }, {});
                resolve(reducedRows);
            });
        });
    }

    updatePostedData(url: string, guid: string) {
        this.db.serialize(() => {
            var stmt = this.db.prepare('REPLACE INTO posted (url, guid) VALUES (?, ?)');
            stmt.run(url, guid);
            stmt.finalize();
        });
    }

    close() {
        this.db.close();
    }
}

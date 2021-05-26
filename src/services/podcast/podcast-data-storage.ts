import { RESOLVER } from 'awilix';
import sqlite3 from 'sqlite3';
import { Podcast } from '../../models/podcast';

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
            function createFeeds(db: sqlite3.Database, callback: Function) {
                db.run(
                    'CREATE TABLE IF NOT EXISTS feeds (id TEXT PRIMARY KEY, url TEXT UNIQUE, title TEXT)',
                    callback,
                );
            }
            function createChannels(db: sqlite3.Database, callback: Function) {
                db.run(
                    'CREATE TABLE IF NOT EXISTS channels (feed_id TEXT, channel_id TEXT, UNIQUE(feed_id, channel_id))',
                    callback,
                );
            }
            function createPosted(db: sqlite3.Database, callback: Function) {
                db.run(
                    'CREATE TABLE IF NOT EXISTS posted (feed_id TEXT PRIMARY KEY UNIQUE, guid TEXT)',
                    callback,
                );
            }

            createFeeds(this.db, () => {
                createChannels(this.db, () => {
                    createPosted(this.db, () => {
                        this.cachePostedDataLocally().then(() => {
                            return resolve();
                        });
                    });
                });
            });
        });
    }

    addFeed(podcast: Podcast, channelId: string): Promise<Dictionary[]> {
        return new Promise((resolve) => {
            this.postedCache[podcast.showFeed] = this.postedCache[podcast.showFeed] || null;

            this.db.run(
                'INSERT OR IGNORE INTO feeds (id, url, title) VALUES (lower(hex(randomblob(3))), ?, ?)',
                [podcast.showFeed, podcast.showTitle],
            );
            this.db.get(
                'SELECT id FROM feeds WHERE url = ? LIMIT 1',
                podcast.showFeed,
                (error, row) => {
                    this.db.run(
                        'INSERT OR IGNORE INTO channels (feed_id, channel_id) VALUES (?, ?)',
                        [row.id, channelId],
                    );
                    this.getFeedsByChannelId(channelId).then((rows) => {
                        resolve(rows);
                    });
                },
            );
        });
    }

    removeFeed(feedId: string, channelId: string): Promise<Dictionary[]> {
        return new Promise((resolve) => {
            this.db.run('DELETE FROM channels WHERE feed_id = ? AND channel_id = ?', [
                feedId,
                channelId,
            ]);
            this.getFeedsByChannelId(channelId).then((rows) => {
                resolve(rows);
            });
        });
    }

    getFeedsByChannelId(channelId: string): Promise<Dictionary[]> {
        return new Promise((resolve) => {
            this.db.all(
                'SELECT id, title FROM feeds INNER JOIN channels ON feeds.id = channels.feed_id WHERE channel_id = ? ORDER BY title',
                channelId,
                (error, rows) => {
                    resolve(rows);
                },
            );
        });
    }

    getChannelsByFeedUrl(feedUrl: string): Promise<Array<string>> {
        return new Promise((resolve) => {
            this.db.all(
                'SELECT channel_id FROM channels c INNER JOIN feeds f ON c.feed_id = f.id WHERE f.url = ?',
                feedUrl,
                (error, rows) => {
                    const reducedRows = rows.reduce((accumulator, current) => {
                        return [...accumulator, current.channel_id];
                    }, []);
                    resolve(reducedRows);
                },
            );
        });
    }

    cachePostedDataLocally(): Promise<void> {
        return new Promise((resolve) => {
            this.db.all(
                'SELECT f.url, p.guid FROM feeds f LEFT JOIN posted p ON f.id = p.feed_id',
                (err, rows) => {
                    const reducedRows = rows.reduce((accumulator, current) => {
                        return { ...accumulator, [current.url]: current.guid };
                    }, {});
                    // Set the local cache
                    this.postedCache = reducedRows;
                    // Resolve promise
                    return resolve();
                },
            );
        });
    }

    getPostedFeeds(): Array<string> {
        return Object.keys(this.postedCache);
    }

    updatePostedData(url: string, guid: string) {
        // Update the local cache
        this.postedCache[url] = guid;

        this.db.get('SELECT id FROM feeds WHERE url = ?', url, (error, row) => {
            this.db.run('REPLACE INTO posted (feed_id, guid) VALUES (?, ?)', [row.id, guid]);
        });
    }

    getPostedFromUrl(url: string): string {
        return this.postedCache[url] || '';
    }

    close() {
        this.db.close();
    }
}

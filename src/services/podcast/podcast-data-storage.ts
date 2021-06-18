import { RESOLVER } from 'awilix';
import bettersqlite3 from 'better-sqlite3';
import { PodcastFeedRow } from '../../models/db/podcast-feed-row.js';
import { PodcastEpisode } from '../../models/podcast-episode.js';

type Dictionary = {
    [key: string]: any;
};

export class PodcastDataStorage {
    static [RESOLVER] = {};

    db: bettersqlite3.Database;
    postedCache: Dictionary;

    constructor(betterSqlite3: typeof bettersqlite3) {
        this.db = betterSqlite3('./db/podcasts.db');
        this.postedCache = {};

        this.setup();
    }

    setup() {
        this.db.exec(
            'CREATE TABLE IF NOT EXISTS feeds (id TEXT PRIMARY KEY, url TEXT UNIQUE, title TEXT)',
        );
        this.db.exec(
            'CREATE TABLE IF NOT EXISTS channels (feed_id TEXT, channel_id TEXT, UNIQUE(feed_id, channel_id))',
        );
        this.db.exec(
            'CREATE TABLE IF NOT EXISTS posted (feed_id TEXT PRIMARY KEY UNIQUE, guid TEXT)',
        );
        this.cachePostedDataLocally();
    }

    cachePostedDataLocally() {
        this.postedCache = this.db
            .prepare('SELECT f.url, p.guid FROM feeds f LEFT JOIN posted p ON f.id = p.feed_id')
            .all()
            .reduce((accumulator, current) => {
                return { ...accumulator, [current.url]: current.guid };
            }, {});
    }

    addFeed(podcastEpisode: PodcastEpisode, channelId: string): PodcastFeedRow[] {
        this.postedCache[podcastEpisode.showFeed] =
            this.postedCache[podcastEpisode.showFeed] || null;
        this.db
            .prepare(
                'INSERT OR IGNORE INTO feeds (id, url, title) VALUES (lower(hex(randomblob(3))), ?, ?)',
            )
            .run(podcastEpisode.showFeed, podcastEpisode.showTitle);
        const feedId =
            this.db
                .prepare('SELECT id FROM feeds WHERE url = ? LIMIT 1')
                .pluck()
                .get(podcastEpisode.showFeed) || '';
        this.db
            .prepare('INSERT OR IGNORE INTO channels (feed_id, channel_id) VALUES (?, ?)')
            .run(feedId, channelId);

        return this.getFeedsByChannelId(channelId);
    }

    removeFeed(feedId: string, channelId: string): PodcastFeedRow[] {
        this.db
            .prepare('DELETE FROM channels WHERE feed_id = ? AND channel_id = ?')
            .run(feedId, channelId);

        return this.getFeedsByChannelId(channelId);
    }

    getFeedCount(): number {
        return Object.keys(this.postedCache).length;
    }

    getFeedsByChannelId(channelId: string): PodcastFeedRow[] {
        return this.db
            .prepare(
                'SELECT id, title FROM feeds INNER JOIN channels ON feeds.id = channels.feed_id WHERE channel_id = ? ORDER BY title',
            )
            .all(channelId)
            .map((dataRow) => {
                const row = new PodcastFeedRow();
                row.id = dataRow.id || '';
                row.title = dataRow.title || '';
                return row;
            });
    }

    getChannelsByFeedUrl(feedUrl: string): Array<string> {
        return this.db
            .prepare(
                'SELECT channel_id FROM channels c INNER JOIN feeds f ON c.feed_id = f.id WHERE f.url = ?',
            )
            .pluck()
            .all(feedUrl);
    }

    getFeedUrlByFeedId(feedId: string): string {
        return this.db.prepare('SELECT url FROM feeds WHERE id = ?').pluck().get(feedId) || '';
    }

    getPostedFeeds(): Array<string> {
        return Object.keys(this.postedCache);
    }

    updatePostedData(url: string, guid: string) {
        // Update the local cache
        this.postedCache[url] = guid;

        const feedId = this.db.prepare('SELECT id FROM feeds WHERE url = ?').pluck().get(url);
        this.db.prepare('REPLACE INTO posted (feed_id, guid) VALUES (?, ?)').run(feedId, guid);
    }

    getPostedFromUrl(url: string): string {
        return this.postedCache[url] || '';
    }

    close() {
        this.db.close();
    }
}

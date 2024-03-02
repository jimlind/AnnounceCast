import * as BetterSqlite3 from 'better-sqlite3';
import { Episode, Podcast } from 'podparse';
// This import specifically needs the js extension because the class is used instead of referenced as a type
import PodcastFeedRow from '../../models/db/podcast-feed-row.js';

interface PodcastDataStorageInterface {
    setup(): void;
    addFeed(podcast: Podcast, channelId: string): void;
    updatePostedData(url: string, episode: Episode): void;
    removeFeed(feedId: string, channelId: string): void;
    getFeedsByChannelId(channelId: string): PodcastFeedRow[];
    getFeedUrlPage(pageNumber: number, pageSize: number): string[];
    getFeedByFeedId(feedId: string): PodcastFeedRow | null;
    getFeedCount(): number;
    getPostedByFeedUrl(url: string): string;
    getChannelsByFeedUrl(url: string): string[];
    resetAllPostedData(): void;
    close(): void;
}

export default class PodcastDataStorage implements PodcastDataStorageInterface {
    private database: BetterSqlite3.Database;
    readonly GUID_LIST_SEPERATOR = '■■■■■■■■■■';

    constructor(betterSqlite3: typeof BetterSqlite3.default) {
        this.database = betterSqlite3('./db/podcasts.db');
    }

    setup() {
        this.database.exec(
            'CREATE TABLE IF NOT EXISTS feeds (id TEXT PRIMARY KEY, url TEXT UNIQUE, title TEXT)',
        );
        this.database.exec(
            'CREATE TABLE IF NOT EXISTS channels (feed_id TEXT, channel_id TEXT, UNIQUE(feed_id, channel_id))',
        );
        this.database.exec(
            'CREATE TABLE IF NOT EXISTS posted (feed_id TEXT PRIMARY KEY UNIQUE, guid TEXT)',
        );
    }

    addFeed(podcast: Podcast, channelId: string): void {
        this.database
            .prepare(
                'INSERT OR IGNORE INTO feeds (id, url, title) VALUES (lower(hex(randomblob(3))), ?, ?)',
            )
            .run(podcast.meta.importFeedUrl, podcast.meta.title);

        const feedId =
            this.database
                .prepare('SELECT id FROM feeds WHERE url = ? LIMIT 1')
                .pluck()
                .get(podcast.meta.importFeedUrl) || '';

        this.database
            .prepare('INSERT OR IGNORE INTO channels (feed_id, channel_id) VALUES (?, ?)')
            .run(feedId, channelId);
    }

    updatePostedData(url: string, episode: Episode): void {
        const feedId = String(
            this.database.prepare('SELECT id FROM feeds WHERE url = ?').pluck().get(url) || '',
        );
        const guid = String(
            this.database
                .prepare('SELECT guid from posted WHERE feed_id = ?')
                .pluck()
                .get(feedId) || '',
        );

        if (!guid.includes(episode.guid)) {
            const guidListString = guid
                .split(this.GUID_LIST_SEPERATOR)
                .concat([episode.guid])
                .filter(Boolean)
                .slice(-5)
                .join(this.GUID_LIST_SEPERATOR);

            this.database
                .prepare('REPLACE INTO posted (feed_id, guid) VALUES (?, ?)')
                .run(feedId, guidListString);
        }
    }

    removeFeed(feedId: string, channelId: string): void {
        this.database
            .prepare('DELETE FROM channels WHERE feed_id = ? AND channel_id = ?')
            .run(feedId, channelId);
    }

    getFeedsByChannelId(channelId: string): PodcastFeedRow[] {
        return this.database
            .prepare(
                'SELECT id, title FROM feeds INNER JOIN channels ON feeds.id = channels.feed_id WHERE channel_id = ? ORDER BY title',
            )
            .all(channelId)
            .map((dataRow: any) => {
                return new PodcastFeedRow(dataRow.id || '', dataRow.title || '');
            });
    }

    getFeedUrlPage(pageNumber: number, pageSize: number): string[] {
        const limit = pageSize;
        const offset = (pageNumber - 1) * pageSize;

        return this.database
            .prepare('SELECT url FROM feeds LIMIT ? OFFSET ?')
            .pluck()
            .all(limit, offset)
            .map(String);
    }

    getFeedByFeedId(feedId: string): PodcastFeedRow | null {
        const data: any = this.database
            .prepare('SELECT id, title FROM feeds WHERE id = ?')
            .all(feedId);

        if (!data) {
            return null;
        }

        return new PodcastFeedRow(data?.id || '', data?.title || '');
    }

    getFeedCount(): number {
        const value = this.database.prepare('SELECT COUNT(id) AS SUM FROM feeds').pluck().get();
        const countNumber = Number(value);
        if (isNaN(countNumber)) {
            return 0;
        }
        return countNumber;
    }

    getPostedByFeedUrl(url: string): string {
        const guid = this.database
            .prepare(
                'SELECT guid FROM posted INNER JOIN feeds ON posted.feed_id = feeds.id WHERE url = ?',
            )
            .pluck()
            .get(url);

        return String(guid || '');
    }

    getChannelsByFeedUrl(url: string): string[] {
        return this.database
            .prepare(
                'SELECT channel_id FROM channels c INNER JOIN feeds f ON c.feed_id = f.id WHERE f.url = ?',
            )
            .pluck()
            .all(url)
            .map(String);
    }

    resetAllPostedData() {
        this.database.prepare("UPDATE posted SET guid = ''").run();
    }

    close() {
        this.database.close();
    }
}

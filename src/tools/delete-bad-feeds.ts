import { Container } from '../container.js';
import bettersqlite3 from 'better-sqlite3';
import { Podcast } from '../models/podcast.js';
import { DiscordConnection } from '../services/discord/discord-connection.js';
import { DiscordDataStorage } from '../services/discord/discord-data-storage.js';
import { PodcastDataStorage } from '../services/podcast/podcast-data-storage.js';
import { PodcastRssProcessor } from '../services/podcast/podcast-rss-processor.js';

const container: Container = new Container('dev');
container.register().then(() => {
    const data = container.resolve<PodcastDataStorage>('podcastDataStorage');
    const feedUrlList = data.getPostedFeeds();

    const bs = container.resolve<typeof bettersqlite3>('betterSqlite3');
    const db = bs('./db/podcasts.db');

    const promiseList = feedUrlList.map((url) =>
        container
            .resolve<PodcastRssProcessor>('podcastRssProcessor')
            .process(url, 1)
            .catch(() => url),
    );
    Promise.all(promiseList)
        .then((resultList) => {
            resultList.forEach((result) => {
                if (result instanceof Podcast) {
                    console.log(`✅ ${result.title} -- ${result.feed}`);
                } else if (typeof result === 'string') {
                    console.log(`❌ ${result}`);
                    //deleteBadDataByUrl(db, result);
                }
            });
        })
        .finally(() => {
            // Close everything down.
            container.resolve<DiscordDataStorage>('discordDataStorage').close();
            container.resolve<PodcastDataStorage>('podcastDataStorage').close();
            container.resolve<DiscordConnection>('discordConnection').getClient().destroy();
        });
});

function deleteBadDataByUrl(db: any, feedUrl: string) {
    const feedIdList = db
        .prepare('SELECT id FROM feeds WHERE url = ? OR url is null')
        .pluck()
        .all(feedUrl);
    const params = feedIdList.map(() => '?').join(',');

    db.prepare(`DELETE FROM posted WHERE feed_id IN (${params})`).run(feedIdList);
    db.prepare(`DELETE FROM channels WHERE feed_id IN (${params})`).run(feedIdList);
    db.prepare(`DELETE FROM feeds WHERE id IN (${params})`).run(feedIdList);
}

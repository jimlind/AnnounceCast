import { Container } from '../container.js';
import { Podcast } from '../models/podcast.js';
import { DiscordConnection } from '../services/discord/discord-connection.js';
import { DiscordDataStorage } from '../services/discord/discord-data-storage.js';
import { PodcastDataStorage } from '../services/podcast/podcast-data-storage.js';
import { PodcastRssProcessor } from '../services/podcast/podcast-rss-processor.js';

const container: Container = new Container();
container.register().then(() => {
    const data = container.resolve<PodcastDataStorage>('podcastDataStorage');
    const feedUrlList = data.getPostedFeeds();

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
                    deleteBadDataByUrl(result);
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

function deleteBadDataByUrl(result: string) {
    container.resolve<PodcastDataStorage>('podcastDataStorage').deleteBadDataByFeedUrl(result);
}

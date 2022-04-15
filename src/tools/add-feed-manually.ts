import { Container } from '../container.js';
import { Podcast } from '../models/podcast.js';
import { PodcastDataStorage } from '../services/podcast/podcast-data-storage';

const podcast = new Podcast();
podcast.feed = 'https://rss.nexx.cloud/QBKHY4RQMECBNN0';
podcast.title = '5 Minuten Harry Podcast von Coldmirror';

const container: Container = new Container('dev');
container.register().then(() => {
    container
        .resolve<PodcastDataStorage>('podcastDataStorage')
        .addFeed(podcast, '851610870751821844');
});

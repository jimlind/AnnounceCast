package jimlind.announcecast.scraper.task;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import java.util.TimerTask;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.scraper.Helper;
import jimlind.announcecast.scraper.Queue;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.PostedFeed;

public class ScrapeSinglePodcast extends TimerTask {
  private final Client client;
  private final Helper helper;
  private final Joined joined;
  private final Queue queue;

  private final String url;

  @AssistedInject
  public ScrapeSinglePodcast(
      Client client, Helper helper, Joined joined, Queue queue, @Assisted String url) {
    this.client = client;
    this.helper = helper;
    this.joined = joined;
    this.queue = queue;
    this.url = url;
  }

  @Override
  public void run() {
    Podcast podcast = client.createPodcastFromFeedUrl(this.url, 1);
    if (podcast == null || podcast.getEpisodeList().isEmpty()) {
      return;
    }

    PostedFeed postedFeed = joined.getPostedFeedByUrl(this.url);
    if (postedFeed == null) {
      return;
    }

    if (this.helper.episodeNotProcessed(podcast.getEpisodeList().getFirst(), postedFeed)) {
      queue.setPodcast(postedFeed.getUrl());
    }
  }
}

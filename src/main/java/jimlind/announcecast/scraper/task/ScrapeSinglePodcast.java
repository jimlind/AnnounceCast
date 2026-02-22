package jimlind.announcecast.scraper.task;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.scraper.Helper;
import jimlind.announcecast.scraper.PodcastQueue;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.PostedFeed;

import java.util.TimerTask;

public class ScrapeSinglePodcast extends TimerTask {
  private final Client client;
  private final Helper helper;
  private final Joined joined;
  private final PodcastQueue podcastQueue;
  private final String url;

  @AssistedInject
  public ScrapeSinglePodcast(
      Client client,
      Helper helper,
      Joined joined,
      PodcastQueue podcastQueue,
      @Assisted String url) {
    this.client = client;
    this.helper = helper;
    this.joined = joined;
    this.podcastQueue = podcastQueue;
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
      podcastQueue.setPodcast(postedFeed.getUrl());
    }
  }
}

package jimlind.announcecast.scraper.task;

import com.google.inject.Inject;
import java.util.List;
import java.util.TimerTask;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.scraper.Helper;
import jimlind.announcecast.scraper.Queue;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.PostedFeed;

public class ScrapeGeneral extends TimerTask {
  private static final int PAGINATION_SIZE = 20;
  @Inject private Client client;
  @Inject private Helper helper;
  @Inject private Joined joined;
  @Inject private Queue queue;
  private int paginationIndex = 0;

  @Override
  public void run() {
    List<PostedFeed> postedFeedList =
        joined.getPaginatedPostedFeed(PAGINATION_SIZE, this.paginationIndex);
    if (postedFeedList == null) {
      this.paginationIndex = 0;
      return;
    }
    this.paginationIndex++;

    for (PostedFeed postedFeed : postedFeedList) {
      Podcast podcast = client.createPodcastFromFeedUrl(postedFeed.getUrl(), 1);
      if (podcast == null || podcast.getEpisodeList().isEmpty()) {
        continue;
      }

      if (helper.episodeNotProcessed(podcast.getEpisodeList().getFirst(), postedFeed)) {
        queue.setPodcast(postedFeed.getUrl());
      }
    }
  }
}

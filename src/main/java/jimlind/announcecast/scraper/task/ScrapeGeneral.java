package jimlind.announcecast.scraper.task;

import java.util.List;
import java.util.TimerTask;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.scraper.Helper;
import jimlind.announcecast.scraper.Queue;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.PostedFeed;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ScrapeGeneral extends TimerTask {
  private static final int PAGINATION_SIZE = 20;
  private final Client client;
  private final Helper helper;
  private final Joined joined;
  private final Queue queue;
  private int paginationIndex = 0;

  @Inject
  public ScrapeGeneral(Client client, Helper helper, Joined joined, Queue queue) {
    this.client = client;
    this.helper = helper;
    this.joined = joined;
    this.queue = queue;
  }

  @Override
  public void run() {
    try {
      executeScrape();
    } catch (Throwable event) {
      log.atError()
          .setMessage("Error running ScrapeGeneral task")
          .addKeyValue("exception", event.getMessage())
          .log();
    }
  }

  private void executeScrape() {
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

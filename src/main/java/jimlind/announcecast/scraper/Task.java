package jimlind.announcecast.scraper;

import com.google.inject.Inject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.PostedFeed;

public class Task {
  int PAGINATION_DELAY = 5000;
  int PAGINATION_SIZE = 20;

  @Inject private Client client;
  @Inject private Joined joined;
  @Inject private Queue queue;

  public void run() {
    TimerTask scrapingTask =
        new TimerTask() {
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
              System.out.println(" xxxx " + postedFeed.getUrl());
              Podcast podcast = client.createPodcastFromFeedUrl(postedFeed.getUrl(), 1);
              if (podcast == null) {
                return;
              }

              if (!postedFeed.getGuid().contains(podcast.getEpisodeList().getFirst().getGuid())) {
                queue.set(postedFeed.getUrl());
              }
            }
          }
        };
    new Timer().schedule(scrapingTask, 0, PAGINATION_DELAY);

    TimerTask queuedTask =
        new TimerTask() {
          @Override
          public void run() {
            // Pull values from the queue
            String url = queue.get();
            if (url != null) {
              Podcast podcast = client.createPodcastFromFeedUrl(url, 1);
              if (podcast == null) {
                return;
              }

              // TODO: When we know we have a good podcast write some info about it.
              System.out.println(podcast.getTitle());
              System.out.println(podcast.getAuthor());

              String description =
                  podcast.getDescription().isBlank()
                      ? podcast.getSummary()
                      : podcast.getDescription();
              System.out.println(description);
            }
          }
        };
    new Timer().schedule(queuedTask, 10, 10);
  }
}

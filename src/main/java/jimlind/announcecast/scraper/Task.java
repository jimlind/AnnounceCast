package jimlind.announcecast.scraper;

import com.google.inject.Inject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import jimlind.announcecast.discord.Manager;
import jimlind.announcecast.discord.message.EpisodeMessage;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Episode;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.PostedFeed;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Slf4j
public class Task {
  int PAGINATION_DELAY = 5000;
  int PAGINATION_SIZE = 20;

  @Inject private Client client;
  @Inject private Joined joined;
  @Inject private Manager manager;
  @Inject private Queue queue;

  public void run() {
    new Timer().schedule(this.scrapingTask(), 0, PAGINATION_DELAY);
    new Timer().schedule(this.queuedTask(), 1000, 10);
  }

  private TimerTask scrapingTask() {
    return new TimerTask() {
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
          if (podcast == null) {
            continue;
          }

          if (postedFeed.getGuid() == null || postedFeed.getGuid().isBlank()) {
            queue.set(postedFeed.getUrl());
            continue;
          }

          if (!postedFeed.getGuid().contains(podcast.getEpisodeList().getFirst().getGuid())) {
            queue.set(postedFeed.getUrl());
          }
        }
      }
    };
  }

  private TimerTask queuedTask() {
    return new TimerTask() {

      @Override
      public void run() {
        // Pull values from the queue, check if podcast with at least one episode exists
        String url = queue.get();
        if (url == null) {
          return;
        }
        Podcast podcast = client.createPodcastFromFeedUrl(url, 1);
        if (podcast == null) {
          return;
        }
        List<Episode> episodeList = podcast.getEpisodeList();
        if (episodeList.isEmpty()) {
          return;
        }

        MessageEmbed message = EpisodeMessage.build(podcast, 0);
        manager.sendMessage("1260736216568168519", message);
      }
    };
  }
}

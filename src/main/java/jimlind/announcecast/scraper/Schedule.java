package jimlind.announcecast.scraper;

import com.google.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import jimlind.announcecast.discord.Manager;
import jimlind.announcecast.discord.message.EpisodeMessage;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Episode;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.scraper.task.ScrapeSubscribers;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.db.Posted;
import jimlind.announcecast.storage.model.PostedFeed;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Slf4j
public class Schedule {
  public static final long SINGLE_PODCAST_PERIOD = TimeUnit.MINUTES.toMillis(1);
  public static final long SUBSCRIBER_SCRAPE_PERIOD = TimeUnit.HOURS.toMillis(2);

  private final int PAGINATION_DELAY = 1000;
  private final int PAGINATION_SIZE = 20;
  @Inject private Channel channel;
  @Inject private Client client;
  @Inject private Helper helper;
  @Inject private Joined joined;
  @Inject private Manager manager;
  @Inject private Posted posted;
  @Inject private Queue queue;

  @Inject private ScrapeSubscribers scrapeSubscribers;

  public void startScrapeQueueWrite() {
    new Timer().schedule(this.scrapeWriteTask(), 0, PAGINATION_DELAY);
  }

  public void startScrapeQueueRead() {
    new Timer().schedule(this.scrapeReadTask(), 1000, 10);
  }

  public void startSubscriberScrapeQueueWrite() {
    new Timer().schedule(scrapeSubscribers, 0, SUBSCRIBER_SCRAPE_PERIOD);
  }

  private TimerTask scrapeWriteTask() {
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
          if (podcast == null || podcast.getEpisodeList().isEmpty()) {
            continue;
          }

          if (helper.episodeNotProcessed(podcast.getEpisodeList().getFirst(), postedFeed)) {
            queue.setPodcast(postedFeed.getUrl());
          }
        }
      }
    };
  }

  private TimerTask scrapeReadTask() {
    return new TimerTask() {

      @Override
      public void run() {
        // Trying to get from the queue will return null if empty so exit early
        String url = queue.getPodcast();
        if (url == null) {
          return;
        }
        Podcast podcast = client.createPodcastFromFeedUrl(url, 4);
        PostedFeed postedFeed = joined.getPostedFeedByUrl(url);
        if (podcast == null || postedFeed == null) {
          return;
        }

        ArrayList<Episode> episodeList = new ArrayList<>();
        for (Episode episode : podcast.getEpisodeList()) {
          // If episode is not processed add it to the list otherwise break the loop to avoid
          // posting old episodes
          if (helper.episodeNotProcessed(episode, postedFeed)) {
            episodeList.add(episode);
          } else break;
          // If posted data is empty break here so only most recent episode is posted
          if (postedFeed.getGuid().isBlank()) {
            break;
          }
        }

        for (Episode episode : episodeList.reversed()) {
          MessageEmbed message = EpisodeMessage.build(podcast, episode);
          queue.setEpisode(postedFeed.getId(), episode.getGuid());
          for (String channelId : channel.getChannelsByFeedId(postedFeed.getId())) {
            manager.sendMessage(
                channelId, message, () -> helper.recordSuccess(postedFeed.getId(), episode));
          }
        }
      }
    };
  }
}

package jimlind.announcecast.scraper;

import com.google.inject.Inject;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import jimlind.announcecast.Helper;
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
  public static final int SINGLE_PODCAST_PERIOD = 1000;

  private static final int SUBSCRIBE_REFRESH_DELAY = 10000;
  private final int PAGINATION_DELAY = 1000;
  private final int PAGINATION_SIZE = 20;
  @Inject private Channel channel;
  @Inject private Client client;
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
    new Timer().schedule(scrapeSubscribers, 0, SUBSCRIBE_REFRESH_DELAY);
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

          if (episodeNotProcessed(podcast.getEpisodeList().getFirst(), postedFeed)) {
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
          if (episodeNotProcessed(episode, postedFeed)) {
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
                channelId, message, () -> recordSuccess(postedFeed.getId(), episode));
          }
        }
      }
    };
  }

  private boolean episodeNotProcessed(Episode episode, PostedFeed postedFeed) {
    // Episode already posted and stored in database
    if (postedFeed.getGuid().contains(episode.getGuid())) {
      return false;
    }

    log.atInfo()
        .setMessage("Guid Not Found")
        .addKeyValue("episodeGuid", episode.getGuid())
        .addKeyValue("postedGuid", postedFeed.getGuid())
        .addKeyValue("postedUrl", postedFeed.getUrl())
        .log();

    // Episode already queued to be posted
    return !this.queue.isEpisodeQueued(postedFeed.getId(), episode.getGuid());
  }

  private synchronized void recordSuccess(String feedId, Episode episode) {
    Duration pubDateDifference =
        Duration.between(Helper.stringToDate(episode.getPubDate()), ZonedDateTime.now());

    log.atInfo()
        .setMessage("Message Send Success Metadata")
        .addKeyValue("pubDate", episode.getPubDate())
        .addKeyValue("publishToPostDifference", pubDateDifference.getSeconds())
        .addKeyValue("isVIP", false)
        .log();

    String separatedGuid = this.posted.getGuidByFeedId(feedId);
    String guid = episode.getGuid();
    if (separatedGuid.contains(guid)) {
      return;
    }

    String separator = "■■■■■■■■■■";
    List<String> guidList = new ArrayList<>(List.of(separatedGuid.split(separator)));
    guidList.add(guid);

    List<String> guidSublist =
        guidList.subList(guidList.size() - Math.min(guidList.size(), 5), guidList.size());
    separatedGuid = String.join(separator, guidSublist);

    this.posted.setGuidByFeed(feedId, separatedGuid);
    this.queue.removeEpisode(feedId, guid);
  }
}

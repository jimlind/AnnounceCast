package jimlind.announcecast.scraper.task;

import jimlind.announcecast.core.taskScheduling.InfiniteFixedRateTask;
import jimlind.announcecast.discord.Manager;
import jimlind.announcecast.discord.message.EpisodeMessage;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Episode;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.scraper.EpisodeCache;
import jimlind.announcecast.scraper.Helper;
import jimlind.announcecast.scraper.PodcastQueue;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.PostedFeed;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class ReadQueue extends InfiniteFixedRateTask {
  private static final long INITIAL_DELAY_MILLIS = 0;
  private static final long PERIOD_MILLIS = 200;
  private static final long TIMEOUT_MILLIS = 10000; // 10 seconds

  private final Channel channel;
  private final Client client;
  private final EpisodeCache episodeCache;
  private final Helper helper;
  private final Joined joined;
  private final Manager manager;
  private final PodcastQueue podcastQueue;

  @Inject
  public ReadQueue(
      Channel channel,
      Client client,
      EpisodeCache episodeCache,
      Helper helper,
      Joined joined,
      Manager manager,
      PodcastQueue podcastQueue) {
    super(INITIAL_DELAY_MILLIS, PERIOD_MILLIS, TIMEOUT_MILLIS);
    this.channel = channel;
    this.client = client;
    this.episodeCache = episodeCache;
    this.helper = helper;
    this.joined = joined;
    this.manager = manager;
    this.podcastQueue = podcastQueue;
  }

  @Override
  public void runTask() {
    // Trying to get from the queue will return null if empty so exit early
    String url = podcastQueue.getPodcast();
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
      // If episode is not processed (or not in the episode queue) add it to the list.
      // Otherwise, break the loop to avoid posting old episodes.
      if (helper.episodeNotProcessed(episode, postedFeed)) {
        episodeList.add(episode);
      } else break;
      // If posted data is empty break here so only most recent episode is posted
      if (postedFeed.getGuid().isBlank()) {
        break;
      }
    }

    // Loop over all episodes reversed (so oldest is first) and log old episodes as success
    // This should help limit episode queue thrashing for podcasts that can't post
    for (Episode episode : episodeList.reversed()) {
      ZonedDateTime pubDateTime = jimlind.announcecast.Helper.stringToDate(episode.getPubDate());
      Duration pubDateDifference = Duration.between(pubDateTime, ZonedDateTime.now());
      if (pubDateDifference.getSeconds() > TimeUnit.HOURS.toSeconds(48)) {
        helper.recordSuccessToDatabase(postedFeed.getId(), episode.getGuid());
      }
    }

    // Loop over all episodes reversed (so oldest is first) attempting to post messages
    for (Episode episode : episodeList.reversed()) {
      episodeCache.setEpisode(postedFeed.getId(), episode.getGuid());
      for (String channelId : channel.getChannelsByFeedId(postedFeed.getId())) {
        // Process the episode for the channel
        List<String> role = this.joined.getTagsByFeedIdAndChannelId(postedFeed.getId(), channelId);
        Stream<String> stream = role.stream().map(input -> String.format("<@&%s>", input));
        String message = stream.collect(Collectors.joining(","));
        MessageEmbed embed = EpisodeMessage.build(podcast, episode);
        manager.sendMessage(
            channelId, message, embed, () -> helper.recordSuccess(postedFeed.getId(), episode));
      }
    }
  }
}

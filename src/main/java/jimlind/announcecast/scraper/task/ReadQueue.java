package jimlind.announcecast.scraper.task;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.discord.Manager;
import jimlind.announcecast.discord.message.EpisodeMessage;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Episode;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.scraper.Helper;
import jimlind.announcecast.scraper.Queue;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.PostedFeed;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Singleton
public class ReadQueue extends TimerTask {
  private final Channel channel;
  private final Client client;
  private final Helper helper;
  private final Joined joined;
  private final Manager manager;
  private final Queue queue;

  @Inject
  public ReadQueue(
      Channel channel, Client client, Helper helper, Joined joined, Manager manager, Queue queue) {
    this.channel = channel;
    this.client = client;
    this.helper = helper;
    this.joined = joined;
    this.manager = manager;
    this.queue = queue;
  }

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
      queue.setEpisode(postedFeed.getId(), episode.getGuid());
      for (String channelId : channel.getChannelsByFeedId(postedFeed.getId())) {

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

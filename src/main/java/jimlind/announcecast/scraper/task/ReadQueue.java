package jimlind.announcecast.scraper.task;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.TimerTask;
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

public class ReadQueue extends TimerTask {
  @Inject private Channel channel;
  @Inject private Client client;
  @Inject private Helper helper;
  @Inject private Manager manager;
  @Inject private Joined joined;
  @Inject private Queue queue;

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
}

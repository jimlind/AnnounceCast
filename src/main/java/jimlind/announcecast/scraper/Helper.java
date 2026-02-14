package jimlind.announcecast.scraper;

import jimlind.announcecast.podcast.Episode;
import jimlind.announcecast.storage.db.Posted;
import jimlind.announcecast.storage.db.PromotedFeed;
import jimlind.announcecast.storage.model.PostedFeed;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class Helper {
  private final Posted posted;
  private final Queue queue;
  private final PromotedFeed promotedFeed;

  @Inject
  public Helper(Posted posted, Queue queue, PromotedFeed promotedFeed) {
    this.posted = posted;
    this.queue = queue;
    this.promotedFeed = promotedFeed;
  }

  public boolean episodeNotProcessed(Episode episode, PostedFeed postedFeed) {
    // Episode already posted and stored in database
    if (postedFeed.getGuid().contains(episode.getGuid() != null ? episode.getGuid() : "")) {
      return false;
    }

    // Episode already queued to be posted
    return !this.queue.isEpisodeQueued(postedFeed.getId(), episode.getGuid());
  }

  public synchronized void recordSuccess(String feedId, Episode episode) {
    Duration pubDateDifference =
        Duration.between(
            jimlind.announcecast.Helper.stringToDate(episode.getPubDate()), ZonedDateTime.now());
    boolean isPromoted = this.promotedFeed.promotedFeedExists(feedId);

    log.atInfo()
        .setMessage("Message Send Success Metadata")
        .addKeyValue("pubDate", episode.getPubDate())
        .addKeyValue("publishToPostDifference", pubDateDifference.getSeconds())
        .addKeyValue("isPromoted", isPromoted)
        .addKeyValue("feedId", feedId)
        .addKeyValue("episodeTitle", episode.getTitle())
        .log();

    recordSuccessLocally(feedId, episode.getGuid());
  }

  public synchronized void recordSuccessLocally(String feedId, String episodeGuid) {
    String separatedGuid = this.posted.getGuidByFeedId(feedId);
    if (separatedGuid.contains(episodeGuid != null ? episodeGuid : "")) {
      return;
    }

    String separator = "■■■■■■■■■■";
    List<String> guidList = new ArrayList<>(List.of(separatedGuid.split(separator)));
    guidList.add(episodeGuid);

    List<String> guidSublist =
        guidList.subList(guidList.size() - Math.min(guidList.size(), 5), guidList.size());
    separatedGuid = String.join(separator, guidSublist);

    this.posted.setGuidByFeed(feedId, separatedGuid);
    this.queue.removeEpisode(feedId, episodeGuid);
  }
}

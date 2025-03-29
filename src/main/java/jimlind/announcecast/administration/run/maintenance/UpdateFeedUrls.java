package jimlind.announcecast.administration.run.maintenance;

import com.google.inject.Inject;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Posted;
import jimlind.announcecast.storage.db.PromotedFeed;

public class UpdateFeedUrls {
  private final Channel channel;
  private final Feed feed;
  private final Posted posted;
  private final PromotedFeed promotedFeed;

  @Inject
  public UpdateFeedUrls(Channel channel, Feed feed, Posted posted, PromotedFeed promotedFeed) {
    this.channel = channel;
    this.feed = feed;
    this.posted = posted;
    this.promotedFeed = promotedFeed;
  }

  public void run() throws Exception {
    for (jimlind.announcecast.storage.model.Feed feed : this.feed.getAllFeeds()) {
      HttpURLConnection connection;
      try {
        URL url = new URI(feed.getUrl()).toURL();
        connection = (java.net.HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.setInstanceFollowRedirects(false);
        int responseCode = connection.getResponseCode();
        if (responseCode == java.net.HttpURLConnection.HTTP_MOVED_PERM) {
          String newUrl = connection.getHeaderField("Location");
          String newFeedId = this.feed.getId(newUrl);
          if (newFeedId.isBlank()) {
            this.feed.setUrlByFeedId(feed.getId(), newUrl);
            continue;
          }

          // Set the new feed id as the source of truth for channels using the old feed id
          this.channel.updateFeedIdByFeedId(feed.getId(), newFeedId);

          // Delete all references to the old feed
          this.feed.deleteFeed(feed.getId());
          this.channel.deleteChannelsByFeedId(feed.getId());
          this.posted.deletePostedByFeedId(feed.getId());
          this.promotedFeed.deletePromotedFeedByFeedId(feed.getId());
        }
      } catch (Exception ignore) {
        // Do nothing
      }
    }
  }
}

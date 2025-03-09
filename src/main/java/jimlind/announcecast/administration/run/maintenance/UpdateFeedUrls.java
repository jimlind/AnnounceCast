package jimlind.announcecast.administration.run.maintenance;

import com.google.inject.Inject;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.storage.db.Feed;

public class UpdateFeedUrls {
  private final Client client;
  private final Feed feed;

  @Inject
  public UpdateFeedUrls(Client client, Feed feed) {
    this.client = client;
    this.feed = feed;
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
          this.feed.setUrlByFeedId(feed.getId(), newUrl);
        }
      } catch (Exception ignore) {
        // Do nothing
      }
    }
  }
}

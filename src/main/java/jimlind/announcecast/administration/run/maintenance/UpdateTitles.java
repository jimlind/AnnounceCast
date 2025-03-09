package jimlind.announcecast.administration.run.maintenance;

import com.google.inject.Inject;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Feed;

public class UpdateTitles {
  private final Client client;
  private final Feed feed;

  @Inject
  public UpdateTitles(Client client, Feed feed) {
    this.client = client;
    this.feed = feed;
  }

  public void run() throws Exception {
    for (jimlind.announcecast.storage.model.Feed feed : this.feed.getAllFeeds()) {
      Podcast podcast = this.client.createPodcastFromFeedUrl(feed.getUrl(), 0);
      if (podcast == null) {
        continue;
      }
      String updatedTitle = podcast.getTitle() == null ? feed.getUrl() : podcast.getTitle().trim();
      this.feed.setTitleByFeedId(feed.getId(), updatedTitle);
    }
  }
}

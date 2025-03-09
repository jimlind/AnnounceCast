package jimlind.announcecast.administration.run.maintenance;

import com.google.inject.Inject;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Subscriber;

public class UpdateTitles {
  private final Client client;
  private final Feed feed;
  private final Subscriber subscriber;

  @Inject
  public UpdateTitles(Client client, Feed feed, Subscriber subscriber) {
    this.client = client;
    this.feed = feed;
    this.subscriber = subscriber;
  }

  public void run() throws Exception {
    for (jimlind.announcecast.storage.model.Feed feed : this.feed.getAllFeeds()) {
      Podcast podcast = this.client.createPodcastFromFeedUrl(feed.getUrl(), 0);
      if (podcast == null) {
        continue;
      }
      String updatedTitle = podcast.getTitle() == null ? feed.getUrl() : podcast.getTitle().trim();

      System.out.println("----------");
      System.out.println(feed.getId());
      System.out.println(updatedTitle);
      //      this.feed.setTitleByFeedId(feed.getId(), updatedTitle);
    }
  }
}

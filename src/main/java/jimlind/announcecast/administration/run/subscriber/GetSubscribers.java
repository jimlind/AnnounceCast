package jimlind.announcecast.administration.run.subscriber;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.Feed;

public class GetSubscribers {
  private final Joined joined;

  @Inject
  public GetSubscribers(Joined joined) {
    this.joined = joined;
  }

  public void run() throws Exception {
    List<Feed> result = this.joined.getPodcastsSubscribed();
    for (Feed feed : result) {
      System.out.println(
          "| " + feed.getId() + " | " + feed.getTitle() + " | " + feed.getUrl() + " |");
    }
  }
}

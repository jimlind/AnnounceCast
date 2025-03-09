package jimlind.announcecast.administration.run.maintenance;

import com.google.inject.Inject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Joined;

public class DeleteUnfollowedFeeds {
  private final Feed feed;
  private final Joined joined;

  @Inject
  DeleteUnfollowedFeeds(Feed feed, Joined joined) {
    this.feed = feed;
    this.joined = joined;
  }

  public void run() throws Exception {
    List<jimlind.announcecast.storage.model.Feed> feedList = this.joined.getFeedsWithoutChannels();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    String outputFile =
        "log/deletes/feed_deletes_unfollowed_" + LocalDateTime.now().format(formatter) + ".txt";
    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

    for (jimlind.announcecast.storage.model.Feed feedModel : feedList) {
      this.feed.deleteFeed(feedModel.getId());
      writer.write("id:" + feedModel.getId() + "|url:" + feedModel.getUrl() + "\n");
    }
    writer.close();
  }
}

package jimlind.announcecast.administration.run.maintenance;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Feed;

@Singleton
public class DeleteUselessFeeds {
  private final Client client;
  private final Feed feed;
  private final Scanner scanner;

  @Inject
  DeleteUselessFeeds(Client client, Feed feed) {
    this.client = client;
    this.feed = feed;
    this.scanner = new Scanner(System.in);
  }

  public void run() throws Exception {
    List<jimlind.announcecast.storage.model.Feed> feedList = this.feed.getAllFeeds();

    for (int i = 0; i < 10; i++) {
      List<jimlind.announcecast.storage.model.Feed> feedStorage = new ArrayList<>();
      for (jimlind.announcecast.storage.model.Feed feedModel : feedList) {
        Podcast podcast = this.client.createPodcastFromFeedUrl(feedModel.getUrl(), 1, i);
        System.out.print("Checking: `" + feedModel.getTitle() + "`...");
        if (podcast == null) {
          feedStorage.add(feedModel);
          System.out.println(" ⛔ No Response (" + feedModel.getUrl() + ")");
          continue;
        }
        if (podcast.getEpisodeList() == null) {
          feedStorage.add(feedModel);
          System.out.println(" ⛔ No Episode List (" + feedModel.getUrl() + ")");
          continue;
        }
        if (podcast.getEpisodeList().isEmpty()) {
          feedStorage.add(feedModel);
          System.out.println(" ⛔ No Episodes (" + feedModel.getUrl() + ")");
          continue;
        }
        System.out.println(" ✅");
      }
      feedList = new ArrayList<>(feedStorage);
      System.out.println("---------");
    }

    System.out.println("Found " + feedList.size() + " unused feeds");
    System.out.print("Delete and archive them? (yes, no): ");
    boolean delete = this.scanner.nextLine().equals("yes");
    if (!delete) {
      return;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    String outputFile =
        "log/deletes/feed_deletes_invalid_" + LocalDateTime.now().format(formatter) + ".txt";
    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

    for (jimlind.announcecast.storage.model.Feed feedModel : feedList) {
      this.feed.deleteFeed(feedModel.getId());
      writer.write("id:" + feedModel.getId() + "|url:" + feedModel.getUrl() + "\n");
    }
    writer.close();
  }
}

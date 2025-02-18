package jimlind.announcecast.administration;

import com.google.inject.Inject;
import java.util.Scanner;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Posted;

public class Command {
  private final Channel channel;
  private final Feed feed;
  private final Posted posted;
  private final Scanner scanner;

  @Inject
  Command(Channel channel, Feed feed, Posted posted) {
    this.channel = channel;
    this.feed = feed;
    this.posted = posted;

    this.scanner = new Scanner(System.in);
  }

  public void run(String[] args) {
    System.out.print("Command? (set-posted, set-vip, purge): ");
    switch (this.scanner.nextLine()) {
      case "set-posted":
        setPostedCommand(args);
        break;
      case "set-vip":
        setVipCommand(args);
        break;
      case "purge":
        purgeCommand(args);
        break;
      default:
        System.out.println("INVALID COMMAND");
        break;
    }
  }

  private void setPostedCommand(String[] args) {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab...): ");
    String feedId = this.scanner.nextLine();

    System.out.print("Posted Value?: ");
    String guid = this.scanner.nextLine();

    this.posted.setGuidByFeed(feedId, guid);
  }

  private void setVipCommand(String[] args) {
    System.out.println("NOT IMPLEMENTED YET");
  }

  private void purgeCommand(String[] args) {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab...): ");
    String feedId = this.scanner.nextLine();

    this.posted.deletePostedByFeedId(feedId);
    this.channel.deleteChannelsByFeedId(feedId);
    this.feed.deleteFeed(feedId);
  }
}

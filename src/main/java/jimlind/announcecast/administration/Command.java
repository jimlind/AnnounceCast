package jimlind.announcecast.administration;

import com.google.inject.Inject;
import java.util.Scanner;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Posted;
import jimlind.announcecast.storage.db.Subscriber;

public class Command {
  private final Channel channel;
  private final Feed feed;
  private final Posted posted;
  private final Scanner scanner;
  private final Subscriber subscriber;

  @Inject
  Command(Channel channel, Feed feed, Posted posted, Subscriber subscriber) {
    this.channel = channel;
    this.feed = feed;
    this.posted = posted;
    this.subscriber = subscriber;

    this.scanner = new Scanner(System.in);
  }

  public void run(String[] args) {
    System.out.print("Command? (set-posted, set-subscriber, purge): ");
    switch (this.scanner.nextLine()) {
      case "set-posted":
        setPostedCommand(args);
        break;
      case "set-subscriber":
        setSubscriberCommand(args);
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

  private void setSubscriberCommand(String[] args) {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab...): ");
    String feedId = this.scanner.nextLine();

    System.out.print("Subscribed? (yes, no): ");
    boolean subscribed = this.scanner.nextLine().equals("yes");

    this.subscriber.setActiveByFeed(feedId, subscribed);
  }

  private void purgeCommand(String[] args) {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab...): ");
    String feedId = this.scanner.nextLine();

    this.posted.deletePostedByFeedId(feedId);
    this.channel.deleteChannelsByFeedId(feedId);
    this.feed.deleteFeed(feedId);
  }
}

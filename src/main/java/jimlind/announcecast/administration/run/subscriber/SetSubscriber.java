package jimlind.announcecast.administration.run.subscriber;

import com.google.inject.Inject;
import java.util.Scanner;
import jimlind.announcecast.storage.db.Subscriber;

public class SetSubscriber {
  private final Subscriber subscriber;
  private final Scanner scanner;

  @Inject
  public SetSubscriber(Subscriber subscriber) {
    this.subscriber = subscriber;
    this.scanner = new Scanner(System.in);
  }

  public void run() throws Exception {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab, ...): ");
    String feedId = this.scanner.nextLine();

    System.out.print("Subscribed? (yes, no): ");
    boolean subscribed = this.scanner.nextLine().equals("yes");

    this.subscriber.setActiveByFeed(feedId, subscribed);
  }
}

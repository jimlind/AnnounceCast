package jimlind.announcecast.administration.run.promotedFeed;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Scanner;
import jimlind.announcecast.storage.db.PromotedFeed;

@Singleton
public class SetPromoted {
  private final PromotedFeed promotedFeed;
  private final Scanner scanner;

  @Inject
  public SetPromoted(PromotedFeed promotedFeed) {
    this.promotedFeed = promotedFeed;
    this.scanner = new Scanner(System.in);
  }

  public void run() throws Exception {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab, ...): ");
    String feedId = this.scanner.nextLine();

    System.out.print("Promoted? (yes, no): ");
    boolean promoted = this.scanner.nextLine().equals("yes");
    if (promoted) {
      this.promotedFeed.addPromotedFeed(feedId, "OVERRIDE");
    } else {
      this.promotedFeed.deletePromotedFeedByFeedId(feedId);
    }
  }
}

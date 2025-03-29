package jimlind.announcecast.administration.run.promotedFeed;

import com.google.inject.Inject;
import java.util.Scanner;
import jimlind.announcecast.storage.db.PromotedFeed;

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

    this.promotedFeed.setActiveByFeed(feedId, promoted);
  }
}

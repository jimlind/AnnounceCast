package jimlind.announcecast.administration;

import com.google.inject.Inject;
import java.util.Scanner;
import jimlind.announcecast.storage.db.Posted;

public class Command {
  private final Posted posted;
  private final Scanner scanner;

  @Inject
  Command(Posted posted) {
    this.scanner = new Scanner(System.in);
    this.posted = posted;
  }

  public void run(String[] args) {

    System.out.print("Command? (set-posted, set-vip): ");
    switch (this.scanner.nextLine()) {
      case "set-posted":
        setPostedCommand(args);
        break;
      case "set-vip":
        setVipCommand(args);
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
}

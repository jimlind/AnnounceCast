package jimlind.announcecast.administration.run.maintenance;

import com.google.inject.Inject;
import java.util.Scanner;

public class Action {
  private final DeleteUnauthorizedChannels deleteUnauthorizedChannels;
  private final DeleteUnfollowedFeeds deleteUnfollowedFeeds;
  private final DeleteUselessFeeds deleteUselessFeeds;
  private final UpdateTitles updateTitles;
  private final Scanner scanner;

  @Inject
  public Action(
      DeleteUnauthorizedChannels deleteUnauthorizedChannels,
      DeleteUnfollowedFeeds deleteUnfollowedFeeds,
      DeleteUselessFeeds deleteUselessFeeds,
      UpdateTitles updateTitles) {
    this.deleteUnauthorizedChannels = deleteUnauthorizedChannels;
    this.deleteUnfollowedFeeds = deleteUnfollowedFeeds;
    this.deleteUselessFeeds = deleteUselessFeeds;
    this.updateTitles = updateTitles;
    this.scanner = new Scanner(System.in);
  }

  public void run() throws Exception {
    System.out.println("Input Number to Choose an Action:");
    System.out.println(" > 1. Update podcast title references");
    System.out.println(" > 2. Delete channels without proper permissions");
    System.out.println(" > 3. Delete dead or empty feeds");
    System.out.println(" > 4. Delete feeds with no following");

    try {
      switch (this.scanner.nextLine()) {
        case "1":
          this.updateTitles.run();
          break;
        case "2":
          this.deleteUnauthorizedChannels.run();
          break;
        case "3":
          this.deleteUselessFeeds.run();
          break;
        case "4":
          this.deleteUnfollowedFeeds.run();
          break;
      }
    } catch (Exception e) {
      System.out.println("FAILED!");
      System.out.println(e);
    }
  }
}

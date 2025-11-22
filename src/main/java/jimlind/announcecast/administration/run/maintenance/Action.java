package jimlind.announcecast.administration.run.maintenance;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Scanner;

@Singleton
public class Action {
  private final DeleteUnauthorizedChannels deleteUnauthorizedChannels;
  private final DeleteUnfollowedFeeds deleteUnfollowedFeeds;
  private final DeleteUselessFeeds deleteUselessFeeds;
  private final UpdateFeedUrls updateFeedUrls;
  private final UpdateTitles updateTitles;
  private final Scanner scanner;

  @Inject
  public Action(
      DeleteUnauthorizedChannels deleteUnauthorizedChannels,
      DeleteUnfollowedFeeds deleteUnfollowedFeeds,
      DeleteUselessFeeds deleteUselessFeeds,
      UpdateFeedUrls updateFeedUrls,
      UpdateTitles updateTitles) {
    this.deleteUnauthorizedChannels = deleteUnauthorizedChannels;
    this.deleteUnfollowedFeeds = deleteUnfollowedFeeds;
    this.deleteUselessFeeds = deleteUselessFeeds;
    this.updateFeedUrls = updateFeedUrls;
    this.updateTitles = updateTitles;
    this.scanner = new Scanner(System.in);
  }

  public void run() throws Exception {
    System.out.println("Input Number to Choose an Action:");
    System.out.println(" > 1. Update podcast title references");
    System.out.println(" > 2. Update podcast URLs via redirects");
    System.out.println(" > 3. Delete channels without proper permissions");
    System.out.println(" > 4. Delete dead or empty feeds");
    System.out.println(" > 5. Delete feeds with no following");

    try {
      switch (this.scanner.nextLine()) {
        case "1":
          this.updateTitles.run();
          break;
        case "2":
          this.updateFeedUrls.run();
          break;
        case "3":
          this.deleteUnauthorizedChannels.run();
          break;
        case "4":
          this.deleteUselessFeeds.run();
          break;
        case "5":
          this.deleteUnfollowedFeeds.run();
          break;
      }
    } catch (Exception e) {
      System.out.println("FAILED!");
      System.out.println(e);
    }
  }
}

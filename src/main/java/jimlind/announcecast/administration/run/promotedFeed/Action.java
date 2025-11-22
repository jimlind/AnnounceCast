package jimlind.announcecast.administration.run.promotedFeed;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Scanner;

@Singleton
public class Action {
  private final GetPromoted getPromoted;
  private final Scanner scanner;
  private final SetPromoted setPromoted;

  @Inject
  public Action(GetPromoted getPromoted, SetPromoted setPromoted) {
    this.getPromoted = getPromoted;
    this.setPromoted = setPromoted;
    this.scanner = new Scanner(System.in);
  }

  public void run() throws Exception {
    System.out.println("Input Number to Choose an Action:");
    System.out.println(" > 1. Sets the active status on a promoted feed row");
    System.out.println(" > 2. Get all active promoted feeds");

    try {
      switch (this.scanner.nextLine()) {
        case "1":
          this.setPromoted.run();
          break;
        case "2":
          this.getPromoted.run();
          break;
      }
    } catch (Exception e) {
      System.out.println("FAILED!");
      System.out.println(e);
    }
  }
}

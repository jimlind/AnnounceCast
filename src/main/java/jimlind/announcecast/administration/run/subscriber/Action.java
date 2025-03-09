package jimlind.announcecast.administration.run.subscriber;

import com.google.inject.Inject;
import java.util.Scanner;

public class Action {
  private final GetSubscribers getSubscribers;
  private final Scanner scanner;
  private final SetSubscriber setSubscriber;

  @Inject
  public Action(GetSubscribers getSubscribers, SetSubscriber setSubscriber) {
    this.getSubscribers = getSubscribers;
    this.setSubscriber = setSubscriber;
    this.scanner = new Scanner(System.in);
  }

  public void run() throws Exception {
    System.out.println("Input Number to Choose an Action:");
    System.out.println(" > 1. Set the active value in a feed's subscriber row");
    System.out.println(" > 2. Get all active subscribers");

    try {
      switch (this.scanner.nextLine()) {
        case "1":
          this.setSubscriber.run();
          break;
        case "2":
          this.getSubscribers.run();
          break;
      }
    } catch (Exception e) {
      System.out.println("FAILED!");
      System.out.println(e);
    }
  }
}

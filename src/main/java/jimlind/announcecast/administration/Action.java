package jimlind.announcecast.administration;

import com.google.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Scanner;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Posted;
import jimlind.announcecast.storage.db.Subscriber;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Action {
  private final Channel channel;
  private final Feed feed;
  private final Helper helper;
  private final Posted posted;
  private final Scanner scanner;
  private final Subscriber subscriber;

  @Inject
  Action(Channel channel, Feed feed, Helper helper, Posted posted, Subscriber subscriber) {
    this.channel = channel;
    this.feed = feed;
    this.helper = helper;
    this.posted = posted;
    this.subscriber = subscriber;

    this.scanner = new Scanner(System.in);
  }

  public void run() {
    System.out.println("Input Number to Choose an Action:");
    System.out.println(" > 1. Set the guid value in a feed's posted row");
    System.out.println(" > 2. Set the active value in a feed's subscriber row");
    System.out.println(" > 3. Purge a feed from the database completely");
    System.out.println(" > 4. Write application's slash commands to Discord");
    System.out.println(" > 5. Delete application's slash commands from Discord");
    System.out.println(" > 6. Delete channels without proper permissions");
    System.out.println(" > 7. Delete dead or empty feeds");
    System.out.println(" > 8. Delete feeds with no following");

    switch (this.scanner.nextLine()) {
      case "1":
        setPostedAction();
        break;
      case "2":
        setSubscriberAction();
        break;
      case "3":
        purgeFeedAction();
        break;
      case "4":
        writeSlashCommands();
        break;
      case "5":
        deleteSlashCommands();
        break;
      case "6":
        deleteUnauthorizedChannels();
        break;
      case "7":
        deleteUselessFeeds();
        break;
      case "8":
        deleteUnfollowedFeeds();
        break;
      default:
        System.out.println("INVALID COMMAND");
        break;
    }
  }

  private void setPostedAction() {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab, ...): ");
    String feedId = this.scanner.nextLine();

    System.out.print("Posted Value?: ");
    String guid = this.scanner.nextLine();

    this.posted.setGuidByFeed(feedId, guid);
  }

  private void setSubscriberAction() {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab, ...): ");
    String feedId = this.scanner.nextLine();

    System.out.print("Subscribed? (yes, no): ");
    boolean subscribed = this.scanner.nextLine().equals("yes");

    this.subscriber.setActiveByFeed(feedId, subscribed);
  }

  private void purgeFeedAction() {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab, ...): ");
    String feedId = this.scanner.nextLine();

    this.posted.deletePostedByFeedId(feedId);
    this.channel.deleteChannelsByFeedId(feedId);
    this.feed.deleteFeed(feedId);
  }

  private void writeSlashCommands() {
    System.out.print("Bot Token? (0PFtbdlKtu...): ");
    String botToken = this.scanner.nextLine();

    try {
      JDA jda = JDABuilder.createLight(botToken).build();
      jda.awaitReady();
      File commandFile = new File("src/main/resources/commands.json");
      List<SlashCommandData> commandList = this.helper.jsonToCommandDataList(commandFile);
      jda.updateCommands().addCommands(commandList).queue();
    } catch (Exception ignore) {
      System.out.print("FAILED");
    }
  }

  private void deleteSlashCommands() {
    System.out.print("Bot Token? (0PFtbdlKtu...): ");
    String botToken = this.scanner.nextLine();

    try {
      JDA jda = JDABuilder.createLight(botToken).build();
      jda.awaitReady();
      jda.updateCommands().complete();
    } catch (Exception ignore) {
      System.out.print("FAILED");
    }
  }

  private void deleteUnauthorizedChannels() {
    System.out.println("Not yet.");
  }

  private void deleteUselessFeeds() {
    System.out.println("Not yet.");
  }

  private void deleteUnfollowedFeeds() {
    System.out.println("Not yet.");
  }
}

package jimlind.announcecast.administration;

import com.google.inject.Inject;
import java.io.File;
import java.util.*;
import jimlind.announcecast.administration.run.SendAllFeeds;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Posted;
import jimlind.announcecast.storage.db.PromotedFeed;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Action {
  private final Channel channel;
  private final Feed feed;
  private final Helper helper;
  private final Posted posted;
  private final Scanner scanner;
  private final SendAllFeeds sendAllFeeds;
  private final PromotedFeed promotedFeed;
  private final jimlind.announcecast.administration.run.maintenance.Action maintenanceAction;
  private final jimlind.announcecast.administration.run.promotedFeed.Action promotedFeedAction;

  @Inject
  Action(
      jimlind.announcecast.administration.run.maintenance.Action maintenanceAction,
      jimlind.announcecast.administration.run.promotedFeed.Action promotedFeedAction,
      Channel channel,
      Feed feed,
      Helper helper,
      Posted posted,
      SendAllFeeds sendAllFeeds,
      PromotedFeed promotedFeed) {
    this.maintenanceAction = maintenanceAction;
    this.promotedFeedAction = promotedFeedAction;
    this.channel = channel;
    this.feed = feed;
    this.helper = helper;
    this.posted = posted;
    this.sendAllFeeds = sendAllFeeds;
    this.promotedFeed = promotedFeed;

    this.scanner = new Scanner(System.in);
  }

  public void run() {
    System.out.println("Input Number to Choose an Action:");
    System.out.println(" > 1. Promoted Feed submenu");
    System.out.println(" > 2. Maintenance submenu");
    System.out.println(" > 3. Set the guid value in a feed's posted row");
    System.out.println(" > 4. Purge a feed from the database completely");
    System.out.println(" > 5. Write application's slash commands to Discord");
    System.out.println(" > 6. Delete application's slash commands from Discord");
    System.out.println(" > 7. Test sending all feeds");

    try {
      switch (this.scanner.nextLine()) {
        case "1":
          this.promotedFeedAction.run();
          break;
        case "2":
          this.maintenanceAction.run();
          break;
        case "3":
          setPostedAction();
          break;
        case "4":
          purgeFeedAction();
          break;
        case "5":
          writeSlashCommands();
          break;
        case "6":
          deleteSlashCommands();
          break;
        case "7":
          this.sendAllFeeds.run();
          break;
        default:
          System.out.println("INVALID COMMAND");
          break;
      }
    } catch (Exception e) {
      System.out.println("FAILED!");
      System.out.println(e);
    }
  }

  private void setPostedAction() {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab, ...): ");
    String feedId = this.scanner.nextLine();

    System.out.print("Posted Value?: ");
    String guid = this.scanner.nextLine();

    this.posted.setGuidByFeed(feedId, guid);
  }

  private void purgeFeedAction() {
    System.out.print("Podcast Feed Id? (8c4aa4, eb1eab, ...): ");
    String feedId = this.scanner.nextLine();

    this.posted.deletePostedByFeedId(feedId);
    this.channel.deleteChannelsByFeedId(feedId);
    this.feed.deleteFeed(feedId);
    this.promotedFeed.deletePromotedFeedByFeedId(feedId);
  }

  private void writeSlashCommands() throws Exception {
    System.out.print("Bot Token? (0PFtbdlKtu...): ");
    String botToken = this.scanner.nextLine();

    JDA jda = JDABuilder.createLight(botToken).build();
    jda.awaitReady();
    File commandFile = new File("src/main/resources/commands.json");
    List<SlashCommandData> commandList = this.helper.jsonToCommandDataList(commandFile);
    jda.updateCommands().addCommands(commandList).queue();
  }

  private void deleteSlashCommands() throws Exception {
    System.out.print("Bot Token? (0PFtbdlKtu...): ");
    String botToken = this.scanner.nextLine();

    JDA jda = JDABuilder.createLight(botToken).build();
    jda.awaitReady();
    jda.updateCommands().complete();
  }
}

package jimlind.announcecast.administration;

import com.google.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import jimlind.announcecast.administration.run.DeleteUnfollowedFeeds;
import jimlind.announcecast.administration.run.SendAllFeeds;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Posted;
import jimlind.announcecast.storage.db.Subscriber;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Action {
  private final DeleteUnfollowedFeeds deleteUnfollowedFeeds;
  private final Channel channel;
  private final Client client;
  private final Feed feed;
  private final Helper helper;
  private final Posted posted;
  private final Scanner scanner;
  private final SendAllFeeds sendAllFeeds;
  private final Subscriber subscriber;

  @Inject
  Action(
      DeleteUnfollowedFeeds deleteUnfollowedFeeds,
      Channel channel,
      Client client,
      Feed feed,
      Helper helper,
      Posted posted,
      SendAllFeeds sendAllFeeds,
      Subscriber subscriber) {
    this.deleteUnfollowedFeeds = deleteUnfollowedFeeds;
    this.channel = channel;
    this.client = client;
    this.feed = feed;
    this.helper = helper;
    this.posted = posted;
    this.sendAllFeeds = sendAllFeeds;
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
    System.out.println(" > 9. Test sending all feeds");

    try {
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
          this.deleteUnfollowedFeeds.run();
          break;
        case "9":
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
    this.subscriber.deleteSubscriberByFeedId(feedId);
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

  private void deleteUnauthorizedChannels() throws Exception {
    System.out.print("Bot Token? (0PFtbdlKtu...): ");
    String botToken = this.scanner.nextLine();

    JDA jda = JDABuilder.createLight(botToken).build();
    jda.awaitReady();

    List<String> channelIdList = this.channel.getUniqueChannelIds();
    for (int i = 0; i < 10; i++) {
      Iterator<String> iterator = channelIdList.iterator();
      while (iterator.hasNext()) {
        boolean hasCorrectPermissions = this.helper.hasCorrectPermissions(jda, iterator.next());
        if (hasCorrectPermissions) {
          iterator.remove();
        }
      }
    }
    System.out.println("Found " + channelIdList.size() + " unauthorized channels");
    System.out.print("Delete and archive them? (yes, no): ");
    boolean delete = this.scanner.nextLine().equals("yes");
    if (!delete) {
      return;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    String outputFile =
        "log/deletes/channel_deletes_" + LocalDateTime.now().format(formatter) + ".txt";
    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

    for (String channelId : channelIdList) {
      System.out.println(channelId);

      for (String feedId : this.channel.getFeedsByChannelId(channelId)) {
        channel.deleteChannel(feedId, channelId);
        writer.write("feed:" + feedId + "|channel:" + channelId + "\n");
      }
    }
    writer.close();
  }

  private void deleteUselessFeeds() throws Exception {
    List<jimlind.announcecast.storage.model.Feed> feedList = this.feed.getAllFeeds();

    for (int i = 0; i < 10; i++) {
      List<jimlind.announcecast.storage.model.Feed> feedStorage = new ArrayList<>();
      for (jimlind.announcecast.storage.model.Feed feedModel : feedList) {
        Podcast podcast = this.client.createPodcastFromFeedUrl(feedModel.getUrl(), 1, i);
        System.out.print("Checking: `" + feedModel.getTitle() + "`...");
        if (podcast == null) {
          feedStorage.add(feedModel);
          System.out.println(" ⛔ No Response (" + feedModel.getUrl() + ")");
          continue;
        }
        if (podcast.getEpisodeList() == null) {
          feedStorage.add(feedModel);
          System.out.println(" ⛔ No Episode List (" + feedModel.getUrl() + ")");
          continue;
        }
        if (podcast.getEpisodeList().isEmpty()) {
          feedStorage.add(feedModel);
          System.out.println(" ⛔ No Episodes (" + feedModel.getUrl() + ")");
          continue;
        }
        System.out.println(" ✅");
      }
      feedList = new ArrayList<>(feedStorage);
      System.out.println("---------");
    }

    System.out.println("Found " + feedList.size() + " unused feeds");
    System.out.print("Delete and archive them? (yes, no): ");
    boolean delete = this.scanner.nextLine().equals("yes");
    if (!delete) {
      return;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    String outputFile =
        "log/deletes/feed_deletes_invalid_" + LocalDateTime.now().format(formatter) + ".txt";
    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

    for (jimlind.announcecast.storage.model.Feed feedModel : feedList) {
      this.feed.deleteFeed(feedModel.getId());
      writer.write("id:" + feedModel.getId() + "|url:" + feedModel.getUrl() + "\n");
    }
    writer.close();
  }
}

package jimlind.announcecast.administration.run;

import com.google.inject.Inject;
import java.util.List;
import java.util.Scanner;
import jimlind.announcecast.discord.EmbedBuilder;
import jimlind.announcecast.discord.Manager;
import jimlind.announcecast.discord.message.EpisodeMessage;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.*;
import jimlind.announcecast.storage.model.PostedFeed;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class SendAllFeeds {
  private final Client client;
  private final Joined joined;
  private final Manager manager;
  private final Scanner scanner;

  @Inject
  SendAllFeeds(Client client, Joined joined, Manager manager) {
    this.client = client;
    this.joined = joined;
    this.manager = manager;

    this.scanner = new Scanner(System.in);
  }

  public void run() throws Exception {
    System.out.print("Bot Token? (0PFtbdlKtu...): ");
    String botToken = this.scanner.nextLine();

    System.out.print("Channel? (1345560192091426949, ...): ");
    String channelId = this.scanner.nextLine();

    this.manager.run(botToken);
    // I could listen for when the manager is ready but dumber easier to wait 3s
    Thread.sleep(3000);

    int paginationIndex = 0;
    while (true) {
      List<PostedFeed> postedFeedList = joined.getPaginatedPostedFeed(4, paginationIndex++);
      if (postedFeedList == null) {
        break;
      }

      for (PostedFeed postedFeed : postedFeedList) {
        MessageEmbed urlMessage = new EmbedBuilder().setDescription(postedFeed.getUrl()).build();
        manager.sendMessage(channelId, urlMessage, () -> {});

        Podcast podcast = client.createPodcastFromFeedUrl(postedFeed.getUrl(), 1);
        if (podcast == null || podcast.getEpisodeList().isEmpty()) {
          continue;
        }
        MessageEmbed message = EpisodeMessage.build(podcast, podcast.getEpisodeList().getFirst());
        manager.sendMessage(channelId, message, () -> {});
      }
    }
  }
}

package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.integration.context.AdminContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Patreon;
import jimlind.announcecast.storage.db.PromotedFeed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class AdminAction {
  @Inject Feed feed;
  @Inject Client client;
  @Inject Patreon patreon;
  @Inject PromotedFeed promotedFeed;

  public AdminContext run(SlashCommandInteractionEvent event) {
    String userId = event.getUser().getId();
    if (!this.patreon.userIdExists(userId)) {
      // Triggers the message telling users they aren't a member
      return new AdminContext(false);
    }

    String action = getOption(event, "action");
    String feedId = getOption(event, "id");
    String roleId = getRoleId(event);

    if (action.equals("set-priority") && !feedId.isBlank()) {
      this.promotedFeed.deletePromotedFeedByUserId(userId);
      this.promotedFeed.addPromotedFeed(feedId, userId);
    } else if (action.equals("set-tag") && !feedId.isBlank() && !roleId.isBlank()) {
      System.out.println("Set Tag");
    } else if (action.equals("display")) {
      // Do Nothing. The default for all actions is displaying data
    } else {
      // Triggers the message telling users the commands
      return new AdminContext(true);
    }

    feedId = this.promotedFeed.getPromotedFeedIdByUserId(userId);
    Podcast podcast = this.client.createPodcastFromFeedUrl(this.feed.getUrl(feedId), 1, 10);

    return new AdminContext(podcast);
  }

  private String getOption(SlashCommandInteractionEvent event, String name) {
    OptionMapping option = event.getInteraction().getOption(name);
    return option != null ? option.getAsString() : "";
  }

  private String getRoleId(SlashCommandInteractionEvent event) {
    OptionMapping option = event.getInteraction().getOption("role");
    return option != null ? option.getAsRole().getId() : "";
  }
}

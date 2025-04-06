package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.integration.context.SettingsContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Patreon;
import jimlind.announcecast.storage.db.PromotedFeed;
import jimlind.announcecast.storage.db.Tag;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SettingsAction {
  @Inject Feed feed;
  @Inject Client client;
  @Inject Patreon patreon;
  @Inject PromotedFeed promotedFeed;
  @Inject Tag tag;

  public SettingsContext run(SlashCommandInteractionEvent event) {
    String userId = event.getUser().getId();
    if (!this.patreon.userIdExists(userId)) {
      // Triggers the message telling users they aren't a member
      return new SettingsContext();
    }

    String feedId = this.promotedFeed.getPromotedFeedIdByUserId(userId);
    Podcast podcast = this.client.createPodcastFromFeedUrl(this.feed.getUrl(feedId), 1, 10);

    return new SettingsContext(podcast);
  }
}

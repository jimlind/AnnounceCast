package jimlind.announcecast.integration.action;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.discord.Manager;
import jimlind.announcecast.integration.PopulatedTag;
import jimlind.announcecast.integration.context.SettingsContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Patreon;
import jimlind.announcecast.storage.db.PromotedFeed;
import jimlind.announcecast.storage.db.Tag;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Singleton
public class SettingsAction {
  private final Feed feed;
  private final Client client;
  private final Manager manager;
  private final Patreon patreon;
  private final PromotedFeed promotedFeed;
  private final Tag tag;

  @Inject
  public SettingsAction(
      Feed feed,
      Client client,
      Manager manager,
      Patreon patreon,
      PromotedFeed promotedFeed,
      Tag tag) {
    this.feed = feed;
    this.client = client;
    this.manager = manager;
    this.patreon = patreon;
    this.promotedFeed = promotedFeed;
    this.tag = tag;
  }

  public SettingsContext run(SlashCommandInteractionEvent event) {
    String userId = event.getUser().getId();
    if (!this.patreon.userIdExists(userId)) {
      // Triggers the message telling users they aren't a member
      return new SettingsContext();
    }

    String feedId = this.promotedFeed.getPromotedFeedIdByUserId(userId);
    Podcast promoted = this.client.createPodcastFromFeedUrl(this.feed.getUrl(feedId), 1, 10);

    List<PopulatedTag> tagList = new ArrayList<>();
    for (jimlind.announcecast.storage.model.Tag tag : this.tag.getTagsByUserId(userId)) {
      String feedUrl = this.feed.getUrl(tag.getFeedId());
      Podcast podcast = this.client.createPodcastFromFeedUrl(feedUrl, 1, 10);
      Role role = this.manager.getRole(tag.getRoleId());
      GuildChannel channel = this.manager.getChannel(tag.getChannelId());

      if (podcast == null || channel == null || role == null) {
        continue;
      }

      PopulatedTag populatedTag = new PopulatedTag();
      populatedTag.setPodcastTitle(podcast.getTitle());
      populatedTag.setRoleName(role.getName());
      populatedTag.setChannelName(channel.getName());
      tagList.add(populatedTag);
    }

    return new SettingsContext(promoted, tagList);
  }
}

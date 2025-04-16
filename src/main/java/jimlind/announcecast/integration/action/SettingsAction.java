package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
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

public class SettingsAction {
  @Inject Feed feed;
  @Inject Client client;
  @Inject Manager manager;
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

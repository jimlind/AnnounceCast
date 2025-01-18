package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

public class FollowRssAction {
  @Inject Client client;
  @Inject Channel channel;
  @Inject Feed feed;

  public @Nullable Podcast run(SlashCommandInteractionEvent event) {
    OptionMapping feedOption = event.getInteraction().getOption("feed");
    String feedUrl = feedOption != null ? feedOption.getAsString() : "";
    Podcast podcast = this.client.createPodcastFromFeedUrl(feedUrl, 0);
    if (podcast == null) {
      return null;
    }

    String feedId = this.feed.addFeed(podcast.getFeedUrl(), podcast.getTitle());
    this.channel.addChannel(feedId, event.getChannelId());

    return podcast;
  }
}

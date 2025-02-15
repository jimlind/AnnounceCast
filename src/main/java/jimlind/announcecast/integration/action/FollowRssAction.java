package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.integration.context.FollowContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Joined;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

public class FollowRssAction {
  @Inject private Client client;
  @Inject private Channel channel;
  @Inject private Feed feed;
  @Inject private Joined joined;

  public FollowContext run(SlashCommandInteractionEvent event) {
    Podcast podcast = this.buildPodcast(event);
    if (podcast != null) {
      String feedId = this.feed.addFeed(podcast.getFeedUrl(), podcast.getTitle());
      this.channel.addChannel(feedId, event.getChannelId());
    }

    return new FollowContext(podcast, this.joined.getFeedsByChannelId(event.getChannel().getId()));
  }

  private @Nullable Podcast buildPodcast(SlashCommandInteractionEvent event) {
    OptionMapping feedOption = event.getInteraction().getOption("feed");
    String feedUrl = feedOption != null ? feedOption.getAsString() : "";

    return this.client.createPodcastFromFeedUrl(feedUrl, 0);
  }
}

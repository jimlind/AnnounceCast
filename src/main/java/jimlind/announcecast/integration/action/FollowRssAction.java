package jimlind.announcecast.integration.action;

import jimlind.announcecast.integration.ActionUtils;
import jimlind.announcecast.integration.context.FollowContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Joined;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FollowRssAction {
  private final Client client;
  private final Channel channel;
  private final Feed feed;
  private final Joined joined;

  @Inject
  public FollowRssAction(Client client, Channel channel, Feed feed, Joined joined) {
    this.client = client;
    this.channel = channel;
    this.feed = feed;
    this.joined = joined;
  }

  public FollowContext run(SlashCommandInteractionEvent event) {
    Podcast podcast = this.buildPodcast(event);
    String channelId = ActionUtils.getChannelId(event);

    if (podcast != null) {
      String feedId = this.feed.addFeed(podcast.getFeedUrl(), podcast.getTitle());
      this.channel.addChannel(feedId, channelId);
    }

    return new FollowContext(podcast, this.joined.getFeedsByChannelId(channelId));
  }

  private @Nullable Podcast buildPodcast(SlashCommandInteractionEvent event) {
    OptionMapping feedOption = event.getInteraction().getOption("feed");
    String feedUrl = feedOption != null ? feedOption.getAsString() : "";

    return this.client.createPodcastFromFeedUrl(feedUrl, 0);
  }
}

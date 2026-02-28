package jimlind.announcecast.integration.action;

import jimlind.announcecast.integration.ActionUtils;
import jimlind.announcecast.integration.context.UnfollowContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.db.Posted;
import jimlind.announcecast.storage.db.PromotedFeed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UnfollowAction {
  private final Channel channel;
  private final Client client;
  private final Feed feed;
  private final Joined joined;
  private final Posted posted;
  private final PromotedFeed promotedFeed;

  @Inject
  public UnfollowAction(
      Channel channel,
      Client client,
      Feed feed,
      Joined joined,
      Posted posted,
      PromotedFeed promotedFeed) {
    this.channel = channel;
    this.client = client;
    this.feed = feed;
    this.joined = joined;
    this.posted = posted;
    this.promotedFeed = promotedFeed;
  }

  public UnfollowContext run(SlashCommandInteractionEvent event) {
    OptionMapping idOption = event.getInteraction().getOption("id");
    String feedId = idOption != null ? idOption.getAsString() : "";

    Podcast podcast = this.buildPodcast(feedId);
    String channelId = ActionUtils.getChannelId(event);

    this.channel.deleteChannel(feedId, channelId);

    // Purge feed from all database tables if there are no more channels
    if (this.channel.getChannelsByFeedId(feedId).isEmpty()) {
      this.feed.deleteFeed(feedId);
      this.posted.deletePostedByFeedId(feedId);
      this.promotedFeed.deletePromotedFeedByFeedId(feedId);
    }

    return new UnfollowContext(podcast, this.joined.getFeedsByChannelId(channelId));
  }

  private @Nullable Podcast buildPodcast(String feedId) {
    String feedUrl = this.feed.getUrl(feedId);

    return this.client.createPodcastFromFeedUrl(feedUrl, 0);
  }
}

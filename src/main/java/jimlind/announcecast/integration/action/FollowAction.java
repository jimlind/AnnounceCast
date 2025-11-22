package jimlind.announcecast.integration.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import jimlind.announcecast.integration.context.FollowContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.ITunes;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Joined;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

@Singleton
public class FollowAction {
  private final Client client;
  private final Channel channel;
  private final Feed feed;
  private final ITunes iTunes;
  private final Joined joined;

  @Inject
  public FollowAction(Client client, Channel channel, Feed feed, ITunes iTunes, Joined joined) {
    this.client = client;
    this.channel = channel;
    this.feed = feed;
    this.iTunes = iTunes;
    this.joined = joined;
  }

  public FollowContext run(SlashCommandInteractionEvent event) {
    Podcast podcast = this.buildPodcast(event);

    if (podcast != null) {
      String feedId = this.feed.addFeed(podcast.getFeedUrl(), podcast.getTitle());
      this.channel.addChannel(feedId, event.getChannelId());
    }

    return new FollowContext(podcast, this.joined.getFeedsByChannelId(event.getChannel().getId()));
  }

  private @Nullable Podcast buildPodcast(SlashCommandInteractionEvent event) {
    OptionMapping keywordsOption = event.getInteraction().getOption("keywords");
    String keywords = keywordsOption != null ? keywordsOption.getAsString() : "";

    List<String> urlList = this.iTunes.search(keywords, 1);
    if (urlList.isEmpty()) {
      return null;
    }

    return this.client.createPodcastFromFeedUrl(urlList.getFirst(), 0);
  }
}

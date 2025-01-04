package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.ITunes;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class FollowAction {
  @Inject Client client;
  @Inject Channel channel;
  @Inject Feed feed;
  @Inject ITunes iTunes;

  public Podcast run(SlashCommandInteractionEvent event) {
    OptionMapping keywordsOption = event.getInteraction().getOption("keywords");
    String keywords = keywordsOption != null ? keywordsOption.getAsString() : "";

    String feed = this.iTunes.search(keywords, 1).getFirst();
    Podcast podcast = this.client.createPodcastFromFeedUrl(feed, 1);
    String feedId = this.feed.addFeed(podcast.getFeedUrl(), podcast.getTitle());
    this.channel.addChannel(feedId, event.getChannelId());

    return podcast;
  }
}

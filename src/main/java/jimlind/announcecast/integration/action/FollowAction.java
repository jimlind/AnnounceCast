package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.ITunes;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

public class FollowAction {
  @Inject Client client;
  @Inject Channel channel;
  @Inject Feed feed;
  @Inject ITunes iTunes;

  public @Nullable Podcast run(SlashCommandInteractionEvent event) {
    OptionMapping keywordsOption = event.getInteraction().getOption("keywords");
    String keywords = keywordsOption != null ? keywordsOption.getAsString() : "";

    List<String> urlList = this.iTunes.search(keywords, 1);
    if (urlList.isEmpty()) {
      return null;
    }

    Podcast podcast = this.client.createPodcastFromFeedUrl(urlList.getFirst(), 0);
    if (podcast == null) {
      return null;
    }

    String feedId = this.feed.addFeed(podcast.getFeedUrl(), podcast.getTitle());
    this.channel.addChannel(feedId, event.getChannelId());

    return podcast;
  }
}

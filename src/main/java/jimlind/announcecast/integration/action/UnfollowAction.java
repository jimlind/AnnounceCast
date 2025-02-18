package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.integration.context.UnfollowContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Joined;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

public class UnfollowAction {
  @Inject Client client;
  @Inject Channel channel;
  @Inject Feed feed;
  @Inject private Joined joined;

  public UnfollowContext run(SlashCommandInteractionEvent event) {
    OptionMapping idOption = event.getInteraction().getOption("id");
    String feedId = idOption != null ? idOption.getAsString() : "";

    Podcast podcast = this.buildPodcast(feedId);
    this.channel.deleteChannel(feedId, event.getChannelId());

    return new UnfollowContext(
        podcast, this.joined.getFeedsByChannelId(event.getChannel().getId()));
  }

  private @Nullable Podcast buildPodcast(String feedId) {
    String feedUrl = this.feed.getUrl(feedId);

    return this.client.createPodcastFromFeedUrl(feedUrl, 0);
  }
}

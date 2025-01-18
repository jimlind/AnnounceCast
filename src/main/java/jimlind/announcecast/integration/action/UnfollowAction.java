package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class UnfollowAction {
  @Inject Client client;
  @Inject Channel channel;
  @Inject Feed feed;

  public Podcast run(SlashCommandInteractionEvent event) {
    OptionMapping idOption = event.getInteraction().getOption("id");
    String feedId = idOption != null ? idOption.getAsString() : "";
    this.channel.removeChannel(feedId, event.getChannelId());

    String feedUrl = this.feed.getUrl(feedId);
    return this.client.createPodcastFromFeedUrl(feedUrl, 0);
  }
}

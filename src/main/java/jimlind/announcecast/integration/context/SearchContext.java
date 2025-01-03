package jimlind.announcecast.integration.context;

import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Stream;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.ITunes;
import jimlind.announcecast.podcast.Podcast;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SearchContext {
  @Inject ITunes iTunes;
  @Inject Client client;

  @Getter private List<Podcast> podcastList;

  public SearchContext build(SlashCommandInteractionEvent event) {
    OptionMapping keywordsOption = event.getInteraction().getOption("keywords");
    String keywords = keywordsOption != null ? keywordsOption.getAsString() : "";

    Stream<String> feedStream = iTunes.search(keywords, 4).stream();
    this.podcastList = feedStream.map(feed -> client.createPodcastFromFeedUrl(feed, 0)).toList();

    return this;
  }
}

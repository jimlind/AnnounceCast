package jimlind.announcecast.integration.action;

import jimlind.announcecast.integration.context.SearchContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.ITunes;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Singleton
public class SearchAction {
  private final ITunes iTunes;
  private final Client client;

  @Inject
  public SearchAction(ITunes iTunes, Client client) {
    this.iTunes = iTunes;
    this.client = client;
  }

  public SearchContext run(SlashCommandInteractionEvent event) {
    OptionMapping keywordsOption = event.getInteraction().getOption("keywords");
    String keywords = keywordsOption != null ? keywordsOption.getAsString() : "";

    Stream<String> feedStream = this.iTunes.search(keywords, 4).stream();
    List<Podcast> podcastList =
        feedStream
            .map(feed -> this.client.createPodcastFromFeedUrl(feed, 0))
            .filter(Objects::nonNull)
            .toList();

    return new SearchContext(podcastList);
  }
}

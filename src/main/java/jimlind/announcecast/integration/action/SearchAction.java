package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Stream;
import jimlind.announcecast.integration.context.SearchContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.ITunes;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SearchAction {
  @Inject private ITunes iTunes;
  @Inject private Client client;

  public SearchContext run(SlashCommandInteractionEvent event) {
    OptionMapping keywordsOption = event.getInteraction().getOption("keywords");
    String keywords = keywordsOption != null ? keywordsOption.getAsString() : "";

    Stream<String> feedStream = this.iTunes.search(keywords, 4).stream();
    List<Podcast> podcastList =
        feedStream.map(feed -> this.client.createPodcastFromFeedUrl(feed, 0)).toList();

    return new SearchContext(podcastList);
  }
}

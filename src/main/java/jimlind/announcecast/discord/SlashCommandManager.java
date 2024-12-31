package jimlind.announcecast.discord;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.ITunes;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SlashCommandManager {
  @Inject private Client client;
  @Inject private ITunes iTunes;

  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    String name = event.getName();
    System.out.println(name);

    if (name.equals("search")) {
      OptionMapping keywordsOption = event.getInteraction().getOption("keywords");
      String keywords = keywordsOption != null ? keywordsOption.getAsString() : "";

      List<String> feedList = iTunes.search(keywords, 3);
      for (String feed : feedList) {
        Podcast podcast = client.createPodcastFromFeedUrl(feed, 0);
        System.out.println(podcast.getTitle());
        System.out.println(podcast.getAuthor());
      }

      event.getHook().sendMessage("Slash!").queue();
    }

    return true;
  }
}

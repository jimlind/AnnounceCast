package jimlind.announcecast.discord.message;

import java.util.ArrayList;
import java.util.List;
import jimlind.announcecast.integration.context.SearchContext;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class SearchMessageList {
  public static List<MessageEmbed> build(SearchContext context) {
    ArrayList<MessageEmbed> messageList = new ArrayList<>();

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setDescription(
        String.format("Displaying %s podcast(s)", context.getPodcastList().size()));
    messageList.add(embedBuilder.build());

    for (Podcast podcast : context.getPodcastList()) {
      messageList.add(PodcastMessage.build(podcast));
    }

    return messageList;
  }
}

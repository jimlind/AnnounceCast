package jimlind.announcecast.discord.message;

import java.util.ArrayList;
import java.util.List;
import jimlind.announcecast.storage.model.Feed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class FeedMessageList {
  public static List<MessageEmbed> build(List<Feed> feedList) {
    ArrayList<MessageEmbed> messageList = new ArrayList<>();

    EmbedBuilder builder = new EmbedBuilder();
    List<String> description = new ArrayList<>(List.of("```", "ID     / TITLE"));

    for (Feed feed : feedList) {
      // This feels a bit "clever" but assures we maximize the space available, by attempting to set
      // the description and if it fails undoing the last element and resetting
      description.add(feed.getId() + " / " + feed.getTitle());
      try {
        builder.setDescription(String.join("\n", description) + "\n```");
      } catch (IllegalArgumentException e) {
        String overflow = description.removeLast();
        builder.setDescription(String.join("\n", description) + "\n```");
        messageList.add(builder.build());

        // Rest description with overflow that causes exception
        description = new ArrayList<>(List.of("```", "ID     / TITLE", overflow));
      }
    }
    if (!builder.isEmpty()) {
      messageList.add(builder.build());
    }

    return messageList;
  }
}

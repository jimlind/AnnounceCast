package jimlind.announcecast.discord.message;

import java.util.List;
import jimlind.announcecast.discord.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class AdminMessageList {
  public static List<MessageEmbed> build() {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    embedBuilder.setTitle("Subscriber Only Dashboard");
    embedBuilder.setDescription("N/A");

    return List.of(embedBuilder.build());
  }
}

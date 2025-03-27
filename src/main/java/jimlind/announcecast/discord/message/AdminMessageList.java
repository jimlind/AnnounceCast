package jimlind.announcecast.discord.message;

import java.util.List;
import jimlind.announcecast.discord.EmbedBuilder;
import jimlind.announcecast.integration.context.AdminContext;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class AdminMessageList {
  public static List<MessageEmbed> build(AdminContext context) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    embedBuilder.setTitle("Subscriber Only Dashboard");
    if (context.isSubscriber()) {
      embedBuilder.setDescription("You are a subscriber.");
    } else {
      embedBuilder.setDescription("You are a not a subscriber.");
    }

    return List.of(embedBuilder.build());
  }
}

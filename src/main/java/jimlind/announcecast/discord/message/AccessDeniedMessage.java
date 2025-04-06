package jimlind.announcecast.discord.message;

import jimlind.announcecast.discord.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class AccessDeniedMessage {
  public static MessageEmbed build() {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    String message =
        """
        You must be a Patreon member to access this command.
        :parking: [Join the Patreon](https://www.patreon.com/AnnounceCast)
        """;
    return embedBuilder.setDescription(message).build();
  }
}

package jimlind.announcecast.discord.message;

import java.util.List;
import jimlind.announcecast.discord.EmbedBuilder;
import jimlind.announcecast.integration.context.AdminContext;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class AdminMessageList {
  public static List<MessageEmbed> build(AdminContext context) {
    if (!context.isSubscriber()) {
      return noAccessMessage();
    }

    // If there is no data to return
    return helpMessage();
  }

  private static List<MessageEmbed> noAccessMessage() {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    String message =
        """
            You must be a subscriber to access this menu.
            :clap: [Join the Patreon](https://www.patreon.com/AnnounceCast)
            """;
    return List.of(embedBuilder.setDescription(message).build());
  }

  private static List<MessageEmbed> helpMessage() {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Subscriber Only Menu");
    embedBuilder.setDescription("There are 4 possible actions you can take using this menu");
    embedBuilder.addField(
        "/admin action:`Set Priority` <id>",
        "Set a feed to receive priority updates using a Podcast Id",
        false);
    embedBuilder.addField(
        "/admin action:`Set Tag` <id> <role>",
        "Set a feed to tag a role using a Podcast Id and Discord Role",
        false);
    embedBuilder.addField(
        "/admin action:`Display Subscriber Data`",
        "Show the data you have saved as a Subscriber",
        false);
    embedBuilder.addField("/admin action:`Help`", "Show this help message", false);

    return List.of(embedBuilder.build());
  }
}

package jimlind.announcecast.discord.message;

import java.util.List;
import jimlind.announcecast.discord.EmbedBuilder;
import jimlind.announcecast.integration.context.SettingsContext;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class SettingsMessageList {
  public static List<MessageEmbed> build(SettingsContext context) {
    if (!context.isPatreonMember()) {
      return List.of(AccessDeniedMessage.build());
    }

    return dataMessage(context.getPodcast());
  }

  private static List<MessageEmbed> dataMessage(Podcast podcast) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Settings");
    String description = "";

    // Prioritized Podcasts
    if (podcast == null) {
      description = "You have not prioritized any podcasts.\n\n";
    } else {
      description =
          "You have prioritized the %s Podcast from %s.\n\n"
              .formatted(podcast.getTitle(), podcast.getAuthor());
    }

    // Tagged Podcasts
    description += "You have not tagged any podcasts.";
    embedBuilder.setDescription(description);

    return List.of(embedBuilder.build());
  }
}

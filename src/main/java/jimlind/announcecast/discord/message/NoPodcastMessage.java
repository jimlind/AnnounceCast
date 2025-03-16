package jimlind.announcecast.discord.message;

import jimlind.announcecast.discord.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class NoPodcastMessage {
  public static MessageEmbed build() {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setDescription("Nothing was found matching your query.");

    return embedBuilder.build();
  }
}

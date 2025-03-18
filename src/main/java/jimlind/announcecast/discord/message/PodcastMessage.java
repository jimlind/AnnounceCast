package jimlind.announcecast.discord.message;

import jimlind.announcecast.Helper;
import jimlind.announcecast.discord.EmbedBuilder;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PodcastMessage {
  public static MessageEmbed build(Podcast podcast) {
    String title = null;
    if (podcast.getTitle() != null && !podcast.getTitle().isBlank()) {
      title = podcast.getTitle();
    }

    String feedLine = "Show Feed URL: " + podcast.getFeedUrl();
    String siteLine = "";
    if (podcast.getShowUrl() != null) {
      siteLine = "\nShow's Website: " + podcast.getShowUrl();
    }

    String description = "";
    if (podcast.getDescription() != null) {
      description = Helper.htmlToMarkdown(podcast.getDescription(), 256) + "\n\n";
    }
    description += feedLine + siteLine;

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(title);
    embedBuilder.setDescription(description);
    embedBuilder.setThumbnail(podcast.getImageUrl());

    if (podcast.getAuthor() != null) {
      embedBuilder.setFooter("Credit: " + podcast.getAuthor());
    }

    return embedBuilder.build();
  }
}

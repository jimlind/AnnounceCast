package jimlind.announcecast.discord.message;

import jimlind.announcecast.Helper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class EpisodeMessage {
  // TODO THIS IS A COPY/PASTE OF PODCAST NOT ACTUALLY EPISODE BUT IT IS A GOOD PLACEHOLDER
  public static MessageEmbed build(jimlind.announcecast.podcast.Podcast podcast) {
    String feedLine = "Show Feed URL: " + podcast.getFeedUrl();

    String siteLine = "";
    if (podcast.getShowUrl() != null) {
      siteLine = "\nShow's Website: " + podcast.getShowUrl();
    }

    String description = Helper.htmlToMarkdown(podcast.getDescription(), 256);
    description += "\n\n" + feedLine + siteLine;

    EmbedBuilder embedBuilder = new EmbedBuilder();

    embedBuilder.setTitle(podcast.getTitle());
    embedBuilder.setDescription(description);
    embedBuilder.setThumbnail(podcast.getImageUrl());

    if (podcast.getAuthor() != null) {
      embedBuilder.setFooter("Credit: " + podcast.getAuthor());
    }

    return embedBuilder.build();
  }
}

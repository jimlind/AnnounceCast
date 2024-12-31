package jimlind.announcecast.discord.message;

import jimlind.announcecast.Helper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Podcast {
  public static MessageEmbed build(jimlind.announcecast.podcast.Podcast podcast) {
    String feedLine = "Show Feed URL: " + podcast.getFeedUrl();
    String siteLine = "Show's Website: " + podcast.getShowUrl();

    String description = Helper.htmlToMarkdown(podcast.getDescription(), 256);
    description += "\n\n" + feedLine + "\n" + siteLine;

    EmbedBuilder embedBuilder = new EmbedBuilder();

    embedBuilder.setTitle(podcast.getTitle());
    embedBuilder.setDescription(description);
    embedBuilder.setThumbnail(podcast.getImageUrl());

    embedBuilder.setFooter("Credit: " + podcast.getAuthor());

    return embedBuilder.build();
  }
}

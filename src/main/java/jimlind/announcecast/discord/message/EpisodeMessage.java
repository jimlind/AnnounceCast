package jimlind.announcecast.discord.message;

import jimlind.announcecast.Helper;
import jimlind.announcecast.discord.UrlHelper;
import jimlind.announcecast.discord.EmbedBuilder;
import jimlind.announcecast.podcast.Episode;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EpisodeMessage {
  public static MessageEmbed build(Podcast podcast, Episode episode) {
    String authorUrl = UrlHelper.createValidUrl(podcast.getShowUrl());
    String authorImageUrl = getAuthorImage(podcast, episode);

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setDescription(getDescriptionText(podcast.getDescription(), episode));
    embedBuilder.setTitle(episode.getTitle(), getTitleLink(episode));
    embedBuilder.setImage(getEpisodeImage(podcast, episode));
    embedBuilder.setAuthor(podcast.getTitle(), authorUrl, authorImageUrl);
    embedBuilder.setFooter(getFooterText(episode));

    return embedBuilder.build();
  }

  private static String getDescriptionText(String defaultValue, Episode episode) {
    String description = episode.getPubDate();
    if (episode.getDescription() != null) {
      description = episode.getDescription();
    } else if (episode.getSummary() != null) {
      description = episode.getSummary();
    } else if (defaultValue != null) {
      description = defaultValue;
    }

    String markdownText = Helper.htmlToMarkdown(description, 512);

    int newlineIndex = markdownText.indexOf('\n');
    if (newlineIndex == -1) {
      return markdownText;
    }

    return markdownText.substring(0, newlineIndex);
  }

  private static @Nullable String getTitleLink(Episode episode) {
    String link = UrlHelper.createValidUrl(episode.getLink());
    if (link == null || link.isBlank()) {
      link = UrlHelper.createValidUrl(episode.getMpegUrl());
    }
    if (link == null || link.isBlank()) {
      link = UrlHelper.createValidUrl(episode.getM4aUrl());
    }
    if (link == null || link.isBlank()) {
      return null;
    }
    return link;
  }

  private static @Nullable String getEpisodeImage(Podcast podcast, Episode episode) {
    String result = UrlHelper.createValidUrl(episode.getImageUrl());
    if (result == null) {
      result = UrlHelper.createValidUrl(episode.getThumbnailUrl());
    }
    if (result == null) {
      result = UrlHelper.createValidUrl(podcast.getImageUrl());
    }

    return result;
  }

  private static @Nullable String getAuthorImage(Podcast podcast, Episode episode) {
    String result = UrlHelper.createValidUrl(podcast.getImageUrl());
    if (result == null) {
      result = UrlHelper.createValidUrl(episode.getImageUrl());
    }
    if (result == null) {
      result = UrlHelper.createValidUrl(episode.getThumbnailUrl());
    }
    return result;
  }

  private static String getDuration(Episode episode) {
    if (episode.getDuration() == null || episode.getDuration().isBlank()) {
      return "";
    }

    int hours;
    int minutes;
    int seconds;

    String[] timeSlices = episode.getDuration().split(":");
    if (timeSlices.length == 3) {
      hours = Integer.parseInt(timeSlices[0]);
      minutes = Integer.parseInt(timeSlices[1]);
      seconds = Integer.parseInt(timeSlices[2]);
    } else if (timeSlices.length == 2) {
      hours = 0;
      minutes = Integer.parseInt(timeSlices[0]);
      seconds = Integer.parseInt(timeSlices[1]);
    } else {
      int totalSeconds = Integer.parseInt(episode.getDuration());
      hours = totalSeconds / 3600;
      minutes = (totalSeconds % 3600) / 60;
      seconds = totalSeconds % 60;
    }

    if (hours > 0) {
      return String.format("%sh %sm %ss", hours, minutes, seconds);
    } else if (minutes > 0) {
      return String.format("%sm %ss", minutes, seconds);
    } else {
      return String.format("%ss", seconds);
    }
  }

  private static String getFooterText(Episode episode) {
    List<String> footerPieces = new ArrayList<>();

    String episodeText = "";
    episodeText += isValid(episode.getSeasonId()) ? "S" + episode.getSeasonId() : "";
    episodeText += isValid(episode.getSeasonId()) && isValid(episode.getEpisodeId()) ? ":" : "";
    episodeText += isValid(episode.getEpisodeId()) ? "E" + episode.getEpisodeId() : "";
    footerPieces.add(episodeText);
    footerPieces.add(getDuration(episode));
    footerPieces.add(
        episode.getExplicit() != null && episode.getExplicit().equals("true")
            ? "Parental Advisory - Explicit Content"
            : "");

    return footerPieces.stream()
        .filter(input -> !input.isEmpty())
        .collect(Collectors.joining(" | "));
  }

  private static boolean isValid(String input) {
    if (input == null) {
      return false;
    }
    return !input.isBlank();
  }
}

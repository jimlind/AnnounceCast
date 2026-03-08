package jimlind.announcecast.discord.message;

import jimlind.announcecast.Helper;
import jimlind.announcecast.core.UrlUtils;
import jimlind.announcecast.discord.EmbedBuilder;
import jimlind.announcecast.podcast.Episode;
import jimlind.announcecast.podcast.Podcast;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class EpisodeMessage {
  @Nullable
  private static String urlValidationWrapper(@Nullable String input) {
    String urlOutput = UrlUtils.rebuild(input);
    if (urlOutput == null && input != null && !input.isBlank()) {
      log.atWarn().setMessage("Unable to parse URL").addKeyValue("input", input).log();
    }

    return urlOutput;
  }

  public static MessageEmbed build(Podcast podcast, Episode episode) {
    String authorUrl = urlValidationWrapper(podcast.getShowUrl());
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

  @Nullable
  private static String getTitleLink(Episode episode) {
    String link = urlValidationWrapper(episode.getLink());
    if (link == null || link.isBlank()) {
      link = urlValidationWrapper(episode.getMpegUrl());
    }
    if (link == null || link.isBlank()) {
      link = urlValidationWrapper(episode.getM4aUrl());
    }
    if (link == null || link.isBlank()) {
      return null;
    }
    return link;
  }

  @Nullable
  private static String getEpisodeImage(Podcast podcast, Episode episode) {
    String result = urlValidationWrapper(episode.getImageUrl());
    if (result == null) {
      result = urlValidationWrapper(episode.getThumbnailUrl());
    }
    if (result == null) {
      result = urlValidationWrapper(podcast.getImageUrl());
    }

    return result;
  }

  @Nullable
  private static String getAuthorImage(Podcast podcast, Episode episode) {
    String result = urlValidationWrapper(podcast.getImageUrl());
    if (result == null) {
      result = urlValidationWrapper(episode.getImageUrl());
    }
    if (result == null) {
      result = urlValidationWrapper(episode.getThumbnailUrl());
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

package jimlind.announcecast.discord.message;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jimlind.announcecast.Helper;
import jimlind.announcecast.podcast.Episode;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

public class EpisodeMessage {
  public static MessageEmbed build(Podcast podcast, Episode episode) {
    String authorUrl = createValidUrl(podcast.getShowUrl());
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
    String link = createValidUrl(episode.getLink());
    if (link == null || link.isBlank()) {
      link = createValidUrl(episode.getMpegUrl());
    }
    if (link == null || link.isBlank()) {
      link = createValidUrl(episode.getM4aUrl());
    }
    if (link == null || link.isBlank()) {
      return null;
    }
    return link;
  }

  private static @Nullable String getEpisodeImage(Podcast podcast, Episode episode) {
    String result = createValidUrl(episode.getImageUrl());
    if (result == null) {
      result = createValidUrl(episode.getThumbnailUrl());
    }
    if (result == null) {
      result = createValidUrl(podcast.getImageUrl());
    }

    return result;
  }

  private static @Nullable String getAuthorImage(Podcast podcast, Episode episode) {
    String result = createValidUrl(podcast.getImageUrl());
    if (result == null) {
      result = createValidUrl(episode.getImageUrl());
    }
    if (result == null) {
      result = createValidUrl(episode.getThumbnailUrl());
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

  private static @Nullable String createValidUrl(@Nullable String input) {
    if (input == null) {
      return null;
    }

    String shortUrl = input.trim();
    if (shortUrl.isBlank()) {
      return null;
    }

    String urlPattern = "^(https?)://([^/]+)(/.*)?$";
    Pattern pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(shortUrl);

    if (matcher.matches()) {
      String protocol = matcher.group(1);
      String host = matcher.group(2);
      // Various DNS services allow underscores but Discord does not
      if (host.contains("_")) {
        return null;
      }

      String path = matcher.group(3) != null ? matcher.group(3) : "";

      return protocol + "://" + host + encodePath(path);
    } else {
      return null;
    }
  }

  private static String encodePath(String path) {
    // Some odd feeds give us garbage that we need to correct
    path = path.replace("&amp;", "&");
    path = path.replace("%3D", "=");

    StringBuilder result = new StringBuilder();
    for (char ch : path.toCharArray()) {
      if (Arrays.asList('/', '?', ',', '=', ':', '&').contains(ch)) {
        result.append(ch);
      } else {
        result.append(URLEncoder.encode(String.valueOf(ch), StandardCharsets.UTF_8));
      }
    }
    return result.toString().replace("+", "%20");
  }
}

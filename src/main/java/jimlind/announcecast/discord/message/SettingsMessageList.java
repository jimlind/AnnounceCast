package jimlind.announcecast.discord.message;

import java.util.List;
import java.util.stream.Collectors;
import jimlind.announcecast.discord.EmbedBuilder;
import jimlind.announcecast.integration.PopulatedTag;
import jimlind.announcecast.integration.context.SettingsContext;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class SettingsMessageList {
  public static List<MessageEmbed> build(SettingsContext context) {
    if (!context.isPatreonMember()) {
      return List.of(AccessDeniedMessage.build());
    }

    return dataMessage(context.getPodcast(), context.getTagList());
  }

  private static List<MessageEmbed> dataMessage(Podcast podcast, List<PopulatedTag> tagList) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Settings");
    StringBuilder description = new StringBuilder();

    // Prioritized Podcasts
    if (podcast == null) {
      description.append("You have not prioritized any podcasts.\n\n");
    } else {
      description.append(formatPodcastString(podcast));
    }

    // Tagged Podcasts
    if (tagList.isEmpty()) {
      description.append("You have not tagged any podcasts.\n\n");
    } else {
      description.append("You have tagged:\n");
      description.append(
          tagList.stream()
              .map(SettingsMessageList::formatTagString)
              .collect(Collectors.joining("\n")));
    }

    embedBuilder.setDescription(description.toString());
    return List.of(embedBuilder.build());
  }

  private static String formatPodcastString(Podcast podcast) {
    String title = podcast.getTitle();
    String author = podcast.getAuthor();

    return "You have prioritized the %s Podcast from %s.\n\n".formatted(title, author);
  }

  private static String formatTagString(PopulatedTag tag) {
    return "\"@%s\" on \"%s\" in \"%s\""
        .formatted(tag.getRoleName(), tag.getPodcastTitle(), tag.getChannelName());
  }
}

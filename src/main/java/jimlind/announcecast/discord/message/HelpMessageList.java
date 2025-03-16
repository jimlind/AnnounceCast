package jimlind.announcecast.discord.message;

import java.util.ArrayList;
import java.util.List;
import jimlind.announcecast.discord.EmbedBuilder;
import jimlind.announcecast.integration.context.HelpContext;
import jimlind.announcecast.podcast.Episode;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class HelpMessageList {
  public static List<MessageEmbed> build(HelpContext context) {
    if (context.getPodcast() != null) {
      return buildTest(context);
    }
    return buildDefault(context);
  }

  public static List<MessageEmbed> buildDefault(HelpContext context) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    // Set title
    embedBuilder.setTitle(
        String.format("%s v%s Documentation", context.getName(), context.getVersion()),
        "https://jimlind.github.io/AnnounceCast/");

    // Set description
    embedBuilder.setDescription(
        String.format(
            "Tracking %s podcasts on %s servers.",
            context.getPodcastCount(), context.getGuildCount()));

    // Set fields for slash commands
    embedBuilder.addField(
        "/follow <keywords> 🔒",
        "Follow a podcast in the channel matching the search keyword(s)",
        false);
    embedBuilder.addField(
        "/follow-rss <feed> 🔒", "Follow a podcast in the channel using an RSS feed", false);
    embedBuilder.addField(
        "/unfollow <id> 🔒", "Unfollow a podcast in the channel using a Podcast Id", false);
    embedBuilder.addField(
        "/following", "Display a list of all podcasts followed in the channel", false);
    embedBuilder.addField(
        "/search <keywords>", "Display up to 4 podcasts matching the search keyword(s)", false);
    embedBuilder.addField(
        "/admin <action> <id> <role>", "Admin commands only available for subscribers", false);
    embedBuilder.addField(
        "/help [test]", "Display this help message optionally sending test message", false);

    // Set fields for permissions data
    String viewStatus = context.isViewChannelEnabled() ? "✅" : "❌";
    String sendStatus = context.isSendMessageEnabled() ? "✅" : "❌";
    String embedStatus = context.isEmbedLinkEnabled() ? "✅" : "❌";
    String permissions =
        String.format(
            "``` %s View channel\n %s Send message in channel/thread\n %s Embed link```",
            viewStatus, sendStatus, embedStatus);
    embedBuilder.addField(":gear:️ Permissions", permissions, false);

    // Set fields for support links
    embedBuilder.addField(
        ":clap: Patreon", "[Support on Patreon](https://www.patreon.com/AnnounceCast)", true);
    embedBuilder.addField(
        ":left_speech_bubble: Discord", "[Join the Discord](https://discord.gg/sEjJTTjG3M)", true);

    return List.of(embedBuilder.build());
  }

  public static List<MessageEmbed> buildTest(HelpContext context) {
    ArrayList<MessageEmbed> messageList = new ArrayList<>();
    EmbedBuilder embedBuilder = new EmbedBuilder();

    embedBuilder.setDescription(
        "The latest episode of Escape Hatch will be published below. If you don't see it you need to adjust your permissions.");
    messageList.add(embedBuilder.build());

    List<Episode> episodeList = context.getPodcast().getEpisodeList();
    if (!episodeList.isEmpty()) {
      messageList.add(EpisodeMessage.build(context.getPodcast(), episodeList.getFirst()));
    }

    return messageList;
  }
}

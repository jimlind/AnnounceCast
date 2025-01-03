package jimlind.announcecast.discord.message;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Help {
  public static MessageEmbed build(
      String name, String version, long podcastCount, long guildCount) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    // Set title
    String title = String.format("%s v%s Documentation", name, version);
    embedBuilder.setTitle(title, "https://jimlind.github.io/AnnounceCast/");

    // Set description
    String description =
        String.format("Tracking %s podcasts on %s servers.", podcastCount, guildCount);
    embedBuilder.setDescription(description);

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
        "/help [test]", "Display this help message optionally sending test messages", false);

    // Set fields for support links
    embedBuilder.addField(
        ":clap: Patreon", "[Support on Patreon](https://www.patreon.com/AnnounceCast)", true);
    embedBuilder.addField(
        ":left_speech_bubble: Discord", "[Join the Discord](https://discord.gg/sEjJTTjG3M)", true);

    return embedBuilder.build();
  }
}

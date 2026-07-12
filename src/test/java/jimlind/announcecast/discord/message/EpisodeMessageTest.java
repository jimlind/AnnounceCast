package jimlind.announcecast.discord.message;

import jimlind.announcecast.podcast.Episode;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.entities.MessageEmbed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class EpisodeMessageTest {

  private static Stream<String> descriptionWithOtherNewlineProvider() {
    return Stream.of(
        "First line",
        "First line<br>Second line",
        "<div>First\nline<br>Second\nline</div>",
        "First line<br /><br />Second line",
        "<p>First line</p><p>Second line</p>",
        "<p>First line</p>\n<p>Second line</p>",
        "First line\nSecond line",
        "First line\u000BSecond line",
        "First line\u000CSecond line",
        "First line\rSecond line",
        "First line\r\nSecond line",
        "First line\u0085Second line",
        "First line\u2028Second line",
        "First line\u2029Second line",
        "First line\rSecond line\nThird line");
  }

  private Episode createEpisode(String description) {
    Episode episode = new Episode();
    episode.setDescription(description);
    return episode;
  }

  @ParameterizedTest
  @MethodSource("descriptionWithOtherNewlineProvider")
  void getDescriptionText_withOtherNewlineCharacters_shouldSplitOnNewline(String description) {
    Podcast podcast = new Podcast();
    Episode episode = createEpisode(description);
    MessageEmbed embed = EpisodeMessage.build(podcast, episode);
    String result = embed.getDescription();

    assertEquals("First line", result);
  }
}

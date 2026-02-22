package jimlind.announcecast.discord;

import net.dv8tion.jda.api.entities.MessageEmbed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class EmbedBuilderTest {
  @Test
  void createEmbedBuilder_withLongTitle_shouldReturnShortTitle() {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    String longText = "a".repeat(300);
    embedBuilder.setTitle(longText);
    MessageEmbed messageEmbed = embedBuilder.build();

    String expected = "a".repeat(253) + "...";
    assertEquals(expected, messageEmbed.getTitle());
  }

  @Test
  void createEmbedBuilder_withShortTitle_shouldReturnSameTitle() {
    String uuid = UUID.randomUUID().toString();

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(uuid);
    MessageEmbed messageEmbed = embedBuilder.build();

    assertEquals(uuid, messageEmbed.getTitle());
  }

  @Test
  void createEmbedBuilder_withShortTitleAndUrl_shouldReturnSameTitle() {
    String uuid = UUID.randomUUID().toString();

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(uuid, "http://example.com");
    MessageEmbed messageEmbed = embedBuilder.build();

    assertEquals(uuid, messageEmbed.getTitle());
  }
}

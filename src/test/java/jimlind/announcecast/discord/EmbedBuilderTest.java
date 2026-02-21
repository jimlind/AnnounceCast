package jimlind.announcecast.discord;

import net.dv8tion.jda.api.entities.MessageEmbed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class EmbedBuilderTest {
  @Test
  void createEmbedBuilder_withLongTitle_shouldThrowException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          EmbedBuilder embedBuilder = new EmbedBuilder();
          String longText = "a".repeat(300);
          embedBuilder.setTitle(longText);
        });
  }

  @Test
  void createEmbedBuilder_withShortTitle_shouldReturnSameTitle() {
    String uuid = UUID.randomUUID().toString();

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(uuid);
    MessageEmbed messageEmbed = embedBuilder.build();
    String title = messageEmbed.getTitle();

    assertEquals(uuid, title);
  }

  @Test
  void createEmbedBuilder_withShortTitleAndUrl_shouldReturnSameTitle() {
    String uuid = UUID.randomUUID().toString();

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(uuid, "http://example.com");
    MessageEmbed messageEmbed = embedBuilder.build();
    String title = messageEmbed.getTitle();

    assertEquals(uuid, title);
  }
}

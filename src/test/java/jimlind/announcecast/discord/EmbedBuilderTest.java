package jimlind.announcecast.discord;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class EmbedBuilderTest {
  @Test
  void createEmbedBuilder_withLongTitle_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          EmbedBuilder embedBuilder = new EmbedBuilder();
          String longText = "a".repeat(300);
          embedBuilder.setTitle(longText);
        });
  }
}

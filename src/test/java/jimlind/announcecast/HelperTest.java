package jimlind.announcecast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

class HelperTest {
  @Test
  void htmlToMarkdown_withBrTag_returnsNewlines() {
    String result = Helper.htmlToMarkdown("First line<br>Second line", 512);
    assertEquals("First line\n\nSecond line", result);
  }

  @Test
  void htmlToMarkdown_withNewlineCharacter_returnsCollapsedText() {
    String result = Helper.htmlToMarkdown("First line\n\nSecond line", 512);
    assertEquals("First line Second line", result);
  }

  @Test
  void htmlToMarkdown_withPTag_returnsNewlines() {
    String result = Helper.htmlToMarkdown("<p>First line</p><p>Second line</p>", 512);
    assertEquals("First line\n\nSecond line", result);
  }

  @Test
  void htmlToMarkdown_withLongInput_shouldTruncateAndAddEllipsis() {
    String longText = "a".repeat(600);
    String result = Helper.htmlToMarkdown(longText, 512);

    assertTrue(result.endsWith("..."));
    assertEquals(512 + 3, result.length());
  }

  @Test
  void stringToDate_shouldParseFirstPattern_whenInputMatchesOffsetFormat() {
    ZonedDateTime expected = ZonedDateTime.of(2024, 6, 5, 10, 15, 30, 0, ZoneOffset.ofHours(2));
    ZonedDateTime actual = Helper.stringToDate("Wed, 05 Jun 2024 10:15:30 +0200");

    assertEquals(expected, actual);
  }

  @Test
  void stringToDate_shouldParseSecondPattern_whenInputMatchesZoneNameFormat() {
    ZonedDateTime expected =
        ZonedDateTime.of(2024, 6, 6, 14, 30, 0, 0, ZoneId.of("America/Los_Angeles"));
    ZonedDateTime actual = Helper.stringToDate("Thu, 06 Jun 2024 14:30:00 PST");

    assertEquals(expected, actual);
  }

  @Test
  void stringToDate_shouldReturnCurrentDateTime_whenInputFormatIsInvalid() {
    ZonedDateTime expected =
        ZonedDateTime.of(2024, 6, 6, 14, 30, 0, 0, ZoneId.of("America/Los_Angeles"));
    ZonedDateTime date = ZonedDateTime.parse("2024-06-06T14:30:00-07:00[America/Los_Angeles]");
    ZonedDateTime actual = null;

    // Mock the static method ZonedDateTime.now() to remove timing oddities
    try (MockedStatic<ZonedDateTime> mock =
        mockStatic(ZonedDateTime.class, Mockito.CALLS_REAL_METHODS)) {
      mock.when(ZonedDateTime::now).thenReturn(date);
      actual = Helper.stringToDate("INVALID DATE STRING");
    }

    assertEquals(expected, actual);
  }
}

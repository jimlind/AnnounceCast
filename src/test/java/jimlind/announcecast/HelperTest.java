package jimlind.announcecast;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class HelperTest {
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

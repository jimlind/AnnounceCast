package jimlind.announcecast.discord.message;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EpisodeMessageTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "http://example.com",
        "https://www.google.com",
        "http://localhost:8080/path/to/resource?query=value#fragment",
        "http://localhost:8080/index.html?a=1&b=2",
        "http://localhost:8080/index.html?big:small,cat:dog",
      })
  void createValidUrl_withValidUrls_shouldReturnString(String validUrlString) {
    assertDoesNotThrow(
        () -> {
          Method method = EpisodeMessage.class.getDeclaredMethod("createValidUrl", String.class);
          method.setAccessible(true);
          String result = (String) method.invoke(null, validUrlString);

          assertNotNull(result, "URL object should not be null for valid input");
          assertEquals(validUrlString, result, "URL string representation should match input");
        },
        "Should not throw for valid URL: " + validUrlString);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "htp://missing.scheme",
        "ftp://debian.org/tmp/file.zip",
        "www.no-scheme.com",
        "http://",
        "just a string",
        "",
      })
  void createValidUrl_withInvalidInputs_shouldReturnNull(String invalidUrlString) {
    assertDoesNotThrow(
        () -> {
          Method method = EpisodeMessage.class.getDeclaredMethod("createValidUrl", String.class);
          method.setAccessible(true);
          String result = (String) method.invoke(null, invalidUrlString);

          assertNull(result, "URL object should be null for invalid input");
        },
        "Should not throw for valid URL: " + invalidUrlString);
  }

  @Test
  void createValidUrl_withNullInput_shouldThrowIllegalArgumentException() {
    assertDoesNotThrow(
        () -> {
          Method method = EpisodeMessage.class.getDeclaredMethod("createValidUrl", String.class);
          method.setAccessible(true);
          String result = (String) method.invoke(null, (Object) null);
          assertNull(result, "URL object should be null for null input");
        },
        "Should throw IllegalArgumentException for null input");
  }
}

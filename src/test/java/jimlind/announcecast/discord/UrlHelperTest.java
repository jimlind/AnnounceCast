package jimlind.announcecast.discord;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;

class UrlHelperTest {

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
          String result = UrlHelper.createValidUrl(validUrlString);
          assertNotNull(result, "URL object should not be null for valid input");
          assertEquals(validUrlString, result, "URL string representation should match input");
        },
        "Should not throw for valid URL: " + validUrlString);
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/urls/data-invalid-urls.csv")
  void createValidUrl_withInvalidInputs_shouldReturnNull(String invalidUrlString) {
    assertDoesNotThrow(
        () -> {
          String result = UrlHelper.createValidUrl(invalidUrlString);
          assertNull(result, "URL object should be null for invalid input");
        },
        "Should not throw for valid URL: " + invalidUrlString);
  }

  @Test
  void createValidUrl_withNullInput_shouldThrowIllegalArgumentException() {
    assertDoesNotThrow(
        () -> {
          String result = UrlHelper.createValidUrl(null);
          assertNull(result, "URL object should be null for null input");
        },
        "Should throw IllegalArgumentException for null input");
  }
}

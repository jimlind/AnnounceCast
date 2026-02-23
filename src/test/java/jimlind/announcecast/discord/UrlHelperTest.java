package jimlind.announcecast.discord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class UrlHelperTest {

  @ParameterizedTest
  @CsvFileSource(resources = "/urls/data-valid.csv")
  void createValidUrl_withValidUrls_shouldReturnSame(String validUrlString) {
    String result = UrlHelper.createValidUrl(validUrlString);
    assertNotNull(result, "URL object should not be null for valid input");
    assertEquals(validUrlString, result, "URL string representation should match input");
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/urls/data-invalid.csv")
  void createValidUrl_withInvalidInputs_shouldReturnNull(String invalidUrlString) {
    String result = UrlHelper.createValidUrl(invalidUrlString);
    assertNull(result, "URL object should be null for invalid input");
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/urls/data-valid-transformed.csv")
  void createValidUrl_withValidUrls_shouldReturnTransformed(
      String validUrl, String transformedUrl) {
    String result = UrlHelper.createValidUrl(validUrl);
    assertNotNull(result, "URL object should not be null for valid input");
    assertEquals(transformedUrl, result, "URL string representation should match input");
  }

  @Test
  void createValidUrl_withNullInput_shouldThrowIllegalArgumentException() {
    String result = UrlHelper.createValidUrl(null);
    assertNull(result, "URL object should be null for null input");
  }
}

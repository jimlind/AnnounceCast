package jimlind.announcecast.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

public class URLTest {
  @ParameterizedTest
  @CsvFileSource(resources = "/urls/data-valid.csv")
  void createValidUrl_withValidUrls_shouldReturnSame(String input) {
    String actual = URL.rebuild(input);
    assertEquals(input, actual, "URL string representation should match input");
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/urls/data-invalid.csv")
  void createValidUrl_withInvalidInputs_shouldReturnNull(String input) {
    String actual = URL.rebuild(input);
    assertNull(actual, "URL object should be null for invalid input");
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/urls/data-valid-transformed.csv")
  void createValidUrl_withValidUrls_shouldReturnTransformed(String input, String expected) {
    String actual = URL.rebuild(input);
    assertEquals(expected, actual, "Transformed URL String is not as expected");
  }
}

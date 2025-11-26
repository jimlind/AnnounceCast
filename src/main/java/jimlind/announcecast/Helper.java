package jimlind.announcecast;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import tools.jackson.databind.ObjectMapper;

public class Helper {

  public static String htmlToMarkdown(String htmlInputString, int length) {
    String markdownSuffix = "";
    if (htmlInputString.length() > length) {
      htmlInputString = htmlInputString.substring(0, length).trim();
      markdownSuffix = "...";
    }

    Document document = Jsoup.parseBodyFragment(htmlInputString);
    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    String markdownText = new CopyDown(options).convert(document.body().toString());

    return markdownText + markdownSuffix;
  }

  public static String objectToString(Object input) {
    if (input instanceof String
        || input instanceof Integer
        || input instanceof Long
        || input instanceof Float
        || input instanceof Double) {
      return input.toString();
    }

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.writeValueAsString(input);
    } catch (Exception e) {
      // Do Nothing. This can fail for a multitude of reasonable reasons
    }

    try {
      return input.toString();
    } catch (Exception e) {
      return e.toString();
    }
  }

  public static ZonedDateTime stringToDate(String input) {
    List<String> datePatterns =
        Arrays.asList("EEE, dd MMM yyyy HH:mm:ss Z", "EEE, dd MMM yyyy HH:mm:ss z");

    ZonedDateTime zonedDateTime = ZonedDateTime.now();
    for (String pattern : datePatterns) {
      try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        zonedDateTime = ZonedDateTime.parse(input, formatter);
        break;
      } catch (DateTimeParseException ignore) {
        // Ignore, try next pattern
      }
    }

    return zonedDateTime;
  }
}

package jimlind.announcecast;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
}

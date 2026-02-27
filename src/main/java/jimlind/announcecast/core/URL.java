package jimlind.announcecast.core;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URL {

  /**
   * I want to be nice to people and systems that write or encode bad URLs and make some good faith
   * guesses about what they want and what I know I can and can't support here.
   *
   * @param input A URL from the user expected to be messy
   * @return A cleaner URL based on a lot of assumptions
   */
  public static String rebuild(String input) {
    String schema = "https";
    String domain = "";
    String path = "";
    String query = "";
    String rest = input;

    Matcher schemaMatcher = Pattern.compile("(.*):+/+(.*)").matcher(input);
    if (schemaMatcher.find()) {
      String cleanSchema = schemaMatcher.group(1).toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
      schema = cleanSchema.isBlank() ? schema : cleanSchema;
      rest = schemaMatcher.group(2);
    }

    if (!schema.matches("(?i)https?")) {
      return null;
    }
    if (rest.isBlank()) {
      return null;
    }

    System.out.println(rest);
    Matcher domainMatcher =
        Pattern.compile("([\\w.:]+)(.*)?(.*)", Pattern.UNICODE_CHARACTER_CLASS).matcher(rest);
    if (domainMatcher.find()) {
      domain = domainMatcher.group(1);
      path = domainMatcher.group(2);
      query = domainMatcher.group(3);
    }

    if (domain.isBlank() || domain.contains("_")) {
      return null;
    }

    return schema + "://" + domain + path + query;
  }
}

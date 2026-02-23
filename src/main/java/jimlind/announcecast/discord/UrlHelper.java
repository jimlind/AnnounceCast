package jimlind.announcecast.discord;

import org.jetbrains.annotations.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlHelper {
  public static @Nullable String createValidUrl(@Nullable String input) {
    if (input == null) {
      return null;
    }

    String shortUrl = input.trim();
    if (shortUrl.isBlank()) {
      return null;
    }

    String urlPattern = "^(https?)://([^/]+)(/.*)?$";
    Pattern pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(shortUrl);

    if (matcher.matches()) {
      String protocol = matcher.group(1);
      String host = matcher.group(2);
      // Various DNS services allow underscores but Discord does not
      if (host.contains("_")) {
        return null;
      }

      // If first character of the host isn't alphanumeric, reject it
      if (host.matches("^[^a-zA-Z0-9].*")) {
        return null;
      }

      String path = matcher.group(3) != null ? matcher.group(3) : "";

      return protocol + "://" + host + encodePath(path);
    } else {
      return null;
    }
  }

  public static String encodePath(String path) {
    // Some odd feeds give us garbage that we need to correct
    path = path.replace("&amp;", "&");
    path = path.replace("%3D", "=");

    StringBuilder result = new StringBuilder();
    for (char ch : path.toCharArray()) {
      if (Arrays.asList('/', '?', ',', '=', ':', '&', '#').contains(ch)) {
        result.append(ch);
      } else {
        result.append(URLEncoder.encode(String.valueOf(ch), StandardCharsets.UTF_8));
      }
    }
    return result.toString().replace("+", "%20");
  }
}

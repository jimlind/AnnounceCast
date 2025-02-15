package jimlind.announcecast.podcast;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ITunes {

  public List<String> search(String keywords, int count) {
    int limit = Math.max(count, 10); // The API acts oddly if the limit is less than 10
    String urlTemplate =
        "https://itunes.apple.com/search?term=%s&country=US&media=podcast&attribute=titleTerm&limit=%s";
    String encodedKeywords = URLEncoder.encode(keywords, StandardCharsets.UTF_8);
    URI uri = URI.create(String.format(urlTemplate, encodedKeywords, limit));

    String title = getClass().getPackage().getImplementationTitle();
    String version = getClass().getPackage().getImplementationVersion();

    try (HttpClient client = HttpClient.newHttpClient()) {
      // Send Request
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(uri)
              .header("User-Agent", title + "/" + version)
              .timeout(Duration.of(1, SECONDS))
              .GET()
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      // Read into a Jackson JsonNode
      JsonNode parentNode = new ObjectMapper().readTree(response.body());
      JsonNode results = parentNode.get("results");
      // Stream to easily map and grab feedUrl
      Stream<JsonNode> nodeStream = StreamSupport.stream(results.spliterator(), false).limit(count);
      return nodeStream.map(node -> node.get("feedUrl").asText()).toList();
    } catch (Exception ignore) {
      return Collections.emptyList();
    }
  }
}

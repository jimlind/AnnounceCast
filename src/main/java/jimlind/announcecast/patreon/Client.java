package jimlind.announcecast.patreon;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Client {

  @Inject
  public Client() {}

  public List<PatreonMember> createMemberList(String patreonAccessToken) {
    // TODO: There is a lot of hardcoded stuff here. Maybe break some of it out?
    String apiUrl =
        "https://www.patreon.com/api/oauth2/v2/campaigns/11731092/members?include=user&fields[user]=full_name,social_connections";
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Authorization", "Bearer " + patreonAccessToken)
            .timeout(Duration.of(5, SECONDS))
            .GET()
            .build();

    // Send the GET request and parse response to string
    String responseString;
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      responseString = response.body();
    } catch (Exception ignore) {
      return List.of();
    }
    if (responseString.isBlank()) {
      return List.of();
    }

    // Parse the string into a specific JsonNode
    JsonNode jsonNode;
    try {
      jsonNode = new ObjectMapper().readTree(responseString).get("included");
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    if (jsonNode == null) {
      return List.of();
    }

    // Build out the Member List
    List<PatreonMember> memberList = new ArrayList<>();
    for (Iterator<JsonNode> it = jsonNode.elements(); it.hasNext(); ) {
      JsonNode member = it.next();
      JsonNode attributes = member.get("attributes");
      JsonNode userId = attributes.get("social_connections").get("discord").get("user_id");
      if (userId == null) {
        continue;
      }

      PatreonMember memberObject = new PatreonMember();
      memberObject.setFullName(attributes.get("full_name").textValue());
      memberObject.setPatreonId(member.get("id").textValue());
      memberObject.setUserId(userId.textValue());
      memberList.add(memberObject);
    }

    return memberList;
  }
}

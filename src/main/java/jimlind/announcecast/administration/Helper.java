package jimlind.announcecast.administration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Helper {
  public List<SlashCommandData> jsonToCommandDataList(File file) throws Exception {
    List<SlashCommandData> commandList = new ArrayList<>();

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode commandsNode = objectMapper.readTree(file);
    for (JsonNode node : commandsNode) {
      String name = node.path("name").asText();
      String description = node.path("description").asText();
      SlashCommandData commandData = Commands.slash(name, description);

      if (node.has("options")) {
        for (JsonNode option : node.path("options")) {
          boolean optionIsBoolean = option.path("type").asText().equals("boolean");
          commandData.addOption(
              optionIsBoolean ? OptionType.BOOLEAN : OptionType.STRING,
              option.path("name").asText(),
              option.path("description").asText(),
              option.path("required").asBoolean(false));
        }
      }
      commandList.add(commandData);
    }

    return commandList;
  }
}

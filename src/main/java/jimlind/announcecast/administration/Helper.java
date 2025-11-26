package jimlind.announcecast.administration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Singleton
public class Helper {
  @Inject
  public Helper() {}

  public List<SlashCommandData> jsonToCommandDataList(File file) throws Exception {
    List<SlashCommandData> commandList = new ArrayList<>();

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode commandsNode = objectMapper.readTree(file);
    for (JsonNode node : commandsNode) {
      String name = node.path("name").asString();
      String description = node.path("description").asString();
      SlashCommandData commandData = Commands.slash(name, description);

      if (node.has("options")) {
        for (JsonNode option : node.path("options")) {
          OptionType type =
              switch (option.path("type").asString()) {
                case "boolean" -> OptionType.BOOLEAN;
                case "role" -> OptionType.ROLE;
                default -> OptionType.STRING;
              };
          OptionData optionData =
              new OptionData(
                  type,
                  option.path("name").asString(),
                  option.path("description").asString(),
                  option.path("required").asBoolean(false));

          if (option.has("choices")) {
            for (JsonNode choice : option.path("choices")) {
              optionData.addChoice(
                  choice.path("label").asString(), choice.path("value").asString());
            }
          }
          commandData.addOptions(optionData);
        }
      }
      commandList.add(commandData);
    }

    return commandList;
  }

  public boolean hasCorrectPermissions(JDA jda, String channelId) {
    // If we can't locate the channel we should exit early
    GuildMessageChannel channel = jda.getChannelById(GuildMessageChannel.class, channelId);
    if (channel == null) {
      return false;
    }

    Member member = channel.getGuild().getSelfMember();
    if (!member.hasPermission(channel, Permission.VIEW_CHANNEL)) {
      return false;
    }

    boolean sendMessageEnabled =
        channel.getType().isThread()
            ? member.hasPermission(channel, Permission.MESSAGE_SEND_IN_THREADS)
            : member.hasPermission(channel, Permission.MESSAGE_SEND);
    if (!sendMessageEnabled) {
      return false;
    }

    return member.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS);
  }
}

package jimlind.announcecast.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DirectMessage {
  private static final String NOT_SUPPORTED_MESSAGE =
      "AnnounceCast Does Not Support Direct Messages";

  public void process(MessageReceivedEvent event) {
    event.getChannel().sendMessage(NOT_SUPPORTED_MESSAGE).queue();
  }

  public void process(SlashCommandInteractionEvent event) {
    event.reply(NOT_SUPPORTED_MESSAGE).queue();
  }
}

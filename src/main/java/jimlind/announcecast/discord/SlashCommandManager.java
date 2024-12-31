package jimlind.announcecast.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommandManager {
  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    String name = event.getName();
    System.out.println(name);

    event.getHook().sendMessage("Slash!").queue();

    return true;
  }
}

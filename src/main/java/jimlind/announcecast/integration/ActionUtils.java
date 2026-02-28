package jimlind.announcecast.integration;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jspecify.annotations.Nullable;

public class ActionUtils {
  public static @Nullable String getChannelId(SlashCommandInteractionEvent event) {
    OptionMapping channelOption = event.getInteraction().getOption("channel");
    if (channelOption == null) {
      return event.getChannelId();
    }

    try {
      return channelOption.getAsChannel().getId();
    } catch (IllegalStateException ignore) {
      return event.getChannelId();
    }
  }
}

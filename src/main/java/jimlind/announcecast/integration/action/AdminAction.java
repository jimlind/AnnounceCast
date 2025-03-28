package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.integration.context.AdminContext;
import jimlind.announcecast.storage.db.Patreon;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class AdminAction {
  @Inject Patreon patreon;

  public AdminContext run(SlashCommandInteractionEvent event) {
    String userId = event.getUser().getId();
    if (!this.patreon.userIdExists(userId)) {
      return new AdminContext(false, null);
    }

    String action = getOption(event, "action");
    String feedId = getOption(event, "id");
    String roleId = getRoleId(event);

    if (action.equals("set-priority") && !feedId.isBlank()) {
      System.out.println("Set Priority");
    } else if (action.equals("set-tag") && !feedId.isBlank() && !roleId.isBlank()) {
      System.out.println("Set Tag");
    } else if (action.equals("display")) {
      System.out.println("Set Tag");
    }

    return new AdminContext(true, action);
  }

  private String getOption(SlashCommandInteractionEvent event, String name) {
    OptionMapping option = event.getInteraction().getOption(name);
    return option != null ? option.getAsString() : "";
  }

  private String getRoleId(SlashCommandInteractionEvent event) {
    OptionMapping option = event.getInteraction().getOption("role");
    return option != null ? option.getAsRole().getId() : "";
  }
}

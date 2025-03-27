package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.integration.context.AdminContext;
import jimlind.announcecast.storage.db.Patreon;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AdminAction {
  @Inject Patreon patreon;

  public AdminContext run(SlashCommandInteractionEvent event) {
    String userId = event.getUser().getId();
    boolean isSubscriber = this.patreon.userIdExists(userId);
    System.out.println(isSubscriber);
    return new AdminContext(isSubscriber);
  }
}

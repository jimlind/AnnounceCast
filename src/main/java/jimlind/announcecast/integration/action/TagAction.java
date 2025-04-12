package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.integration.context.SettingsContext;
import jimlind.announcecast.storage.db.Patreon;
import jimlind.announcecast.storage.db.Tag;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class TagAction {
  @Inject Patreon patreon;
  @Inject SettingsAction settingsAction;
  @Inject Tag tag;

  public SettingsContext run(SlashCommandInteractionEvent event) {
    String userId = event.getUser().getId();
    if (!this.patreon.userIdExists(userId)) {
      // Triggers the message telling users they aren't a member
      return new SettingsContext();
    }

    OptionMapping idOption = event.getInteraction().getOption("id");
    String feedId = idOption != null ? idOption.getAsString() : "";

    OptionMapping roleOption = event.getInteraction().getOption("role");
    String roleId = roleOption != null ? roleOption.getAsRole().getId() : "";

    String channelId = event.getChannelId();
    if (roleId.isEmpty()) {
      this.tag.removeTags(feedId, channelId);
    } else {
      this.tag.addTag(feedId, roleId, channelId, userId);
    }

    return this.settingsAction.run(event);
  }
}

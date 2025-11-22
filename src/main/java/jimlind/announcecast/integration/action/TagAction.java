package jimlind.announcecast.integration.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.integration.context.SettingsContext;
import jimlind.announcecast.storage.db.Patreon;
import jimlind.announcecast.storage.db.Tag;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@Singleton
public class TagAction {
  private final Patreon patreon;
  private final SettingsAction settingsAction;
  private final Tag tag;

  @Inject
  public TagAction(Patreon patreon, SettingsAction settingsAction, Tag tag) {
    this.patreon = patreon;
    this.settingsAction = settingsAction;
    this.tag = tag;
  }

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

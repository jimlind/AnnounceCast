package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.integration.context.SettingsContext;
import jimlind.announcecast.storage.db.Patreon;
import jimlind.announcecast.storage.db.PromotedFeed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PrioritizeAction {
  @Inject Patreon patreon;
  @Inject PromotedFeed promotedFeed;
  @Inject SettingsAction settingsAction;

  public SettingsContext run(SlashCommandInteractionEvent event) {
    String userId = event.getUser().getId();
    if (!this.patreon.userIdExists(userId)) {
      // Triggers the message telling users they aren't a member
      return new SettingsContext();
    }

    OptionMapping idOption = event.getInteraction().getOption("id");
    String feedId = idOption != null ? idOption.getAsString() : "";
    this.promotedFeed.deletePromotedFeedByUserId(userId);
    this.promotedFeed.addPromotedFeed(feedId, userId);

    return this.settingsAction.run(event);
  }
}

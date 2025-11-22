package jimlind.announcecast.integration.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.integration.context.SettingsContext;
import jimlind.announcecast.storage.db.Patreon;
import jimlind.announcecast.storage.db.PromotedFeed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@Singleton
public class PrioritizeAction {
  private final Patreon patreon;
  private final PromotedFeed promotedFeed;
  private final SettingsAction settingsAction;

  @Inject
  public PrioritizeAction(Patreon patreon, PromotedFeed promotedFeed, SettingsAction settingsAction) {
    this.patreon = patreon;
    this.promotedFeed = promotedFeed;
    this.settingsAction = settingsAction;
  }

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

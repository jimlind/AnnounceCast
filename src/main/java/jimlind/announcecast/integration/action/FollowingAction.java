package jimlind.announcecast.integration.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import jimlind.announcecast.integration.context.FollowingContext;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.Feed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Singleton
public class FollowingAction {
  private final Joined joined;

  @Inject
  public FollowingAction(Joined joined) {
    this.joined = joined;
  }

  public FollowingContext run(SlashCommandInteractionEvent event) {
    List<Feed> feedList = this.joined.getFeedsByChannelId(event.getChannel().getId());

    return new FollowingContext(feedList);
  }
}

package jimlind.announcecast.integration.action;

import jimlind.announcecast.integration.ActionUtils;
import jimlind.announcecast.integration.context.FollowingContext;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.Feed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class FollowingAction {
  private final Joined joined;

  @Inject
  public FollowingAction(Joined joined) {
    this.joined = joined;
  }

  public FollowingContext run(SlashCommandInteractionEvent event) {
    List<Feed> feedList = this.joined.getFeedsByChannelId(ActionUtils.getChannelId(event));

    return new FollowingContext(feedList);
  }
}

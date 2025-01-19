package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.integration.context.FollowingContext;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.Feed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class FollowingAction {
  @Inject private Joined joined;

  public FollowingContext run(SlashCommandInteractionEvent event) {
    List<Feed> feedList = this.joined.getFeedsByChannelId(event.getChannel().getId());

    return new FollowingContext(feedList);
  }
}

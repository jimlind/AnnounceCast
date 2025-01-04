package jimlind.announcecast.integration.context;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.Feed;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class FollowingContext {
  @Getter private List<Feed> feedList;

  @Inject private Joined joined;

  public FollowingContext build(SlashCommandInteractionEvent event) {
    this.feedList = this.joined.getFeedsByChannelId(event.getChannel().getId());

    return this;
  }
}

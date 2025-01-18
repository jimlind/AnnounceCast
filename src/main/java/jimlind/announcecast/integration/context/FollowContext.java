package jimlind.announcecast.integration.context;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.Feed;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

public class FollowContext {
  @Getter private List<Feed> feedList;
  @Getter private @Nullable Podcast podcast;

  @Inject private Joined joined;

  public FollowContext build(SlashCommandInteractionEvent event, Podcast podcast) {
    this.feedList = this.joined.getFeedsByChannelId(event.getChannel().getId());
    this.podcast = podcast;

    return this;
  }
}

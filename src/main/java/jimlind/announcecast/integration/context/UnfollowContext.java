package jimlind.announcecast.integration.context;

import java.util.List;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.model.Feed;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class UnfollowContext {
  private final @Nullable Podcast podcast;
  private final List<Feed> feedList;

  public UnfollowContext(@Nullable Podcast podcast, List<Feed> feedList) {
    this.podcast = podcast;
    this.feedList = feedList;
  }
}

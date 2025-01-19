package jimlind.announcecast.integration.context;

import java.util.List;
import jimlind.announcecast.storage.model.Feed;
import lombok.Getter;

@Getter
public class FollowingContext {
  private final List<Feed> feedList;

  public FollowingContext(List<Feed> feedList) {
    this.feedList = feedList;
  }
}

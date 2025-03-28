package jimlind.announcecast.integration.context;

import lombok.Getter;

@Getter
public class AdminContext {

  private final boolean isSubscriber;
  private final String action;

  public AdminContext(boolean isSubscriber, String action) {
    this.isSubscriber = isSubscriber;
    this.action = action;
  }
}

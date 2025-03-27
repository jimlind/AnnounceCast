package jimlind.announcecast.integration.context;

import lombok.Getter;

@Getter
public class AdminContext {

  private final boolean isSubscriber;

  public AdminContext(boolean isSubscriber) {
    this.isSubscriber = isSubscriber;
  }
}

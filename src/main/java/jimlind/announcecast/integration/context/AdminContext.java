package jimlind.announcecast.integration.context;

import lombok.Getter;

@Getter
public class AdminContext {

  private final boolean isPatreonMember;
  private final String action;

  public AdminContext(boolean isPatreonMember, String action) {
    this.isPatreonMember = isPatreonMember;
    this.action = action;
  }
}

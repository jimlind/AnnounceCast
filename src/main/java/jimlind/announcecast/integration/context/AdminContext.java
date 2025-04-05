package jimlind.announcecast.integration.context;

import jimlind.announcecast.podcast.Podcast;
import lombok.Getter;

@Getter
public class AdminContext {

  private final boolean patreonMember;
  private final Podcast podcast;
  private boolean onlyHelpMessage = false;

  public AdminContext(boolean patreonMember) {
    this.patreonMember = patreonMember;
    this.podcast = null;
    this.onlyHelpMessage = true;
  }

  public AdminContext(Podcast podcast) {
    this.patreonMember = true;
    this.podcast = podcast;
    this.onlyHelpMessage = false;
  }
}

package jimlind.announcecast.integration.context;

import jimlind.announcecast.podcast.Podcast;
import lombok.Getter;

@Getter
public class SettingsContext {

  private final boolean patreonMember;
  private final Podcast podcast;

  public SettingsContext() {
    this.patreonMember = false;
    this.podcast = null;
  }

  public SettingsContext(Podcast podcast) {
    this.patreonMember = true;
    this.podcast = podcast;
  }
}

package jimlind.announcecast.integration.context;

import java.util.List;
import jimlind.announcecast.integration.PopulatedTag;
import jimlind.announcecast.podcast.Podcast;
import lombok.Getter;

@Getter
public class SettingsContext {

  private final boolean patreonMember;
  private final Podcast podcast;
  private final List<PopulatedTag> tagList;

  public SettingsContext() {
    this.patreonMember = false;
    this.podcast = null;
    this.tagList = List.of();
  }

  public SettingsContext(Podcast podcast, List<PopulatedTag> tagList) {
    this.patreonMember = true;
    this.podcast = podcast;
    this.tagList = tagList;
  }
}

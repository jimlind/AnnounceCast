package jimlind.announcecast.integration.context;

import java.util.List;
import jimlind.announcecast.podcast.Podcast;
import lombok.Getter;

@Getter
public class SearchContext {
  private final List<Podcast> podcastList;

  public SearchContext(List<Podcast> podcastList) {
    this.podcastList = podcastList;
  }
}

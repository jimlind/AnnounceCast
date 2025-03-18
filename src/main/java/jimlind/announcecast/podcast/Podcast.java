package jimlind.announcecast.podcast;

import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class Podcast {
  private @Nullable String title;
  private @Nullable String description;
  private String summary;
  private String feedUrl;
  private String showUrl;
  private String imageUrl;
  private String author;
  private List<Episode> episodeList = new LinkedList<>();

  public void addEpisode(Episode episode) {
    this.episodeList.add(episode);
  }

  public boolean isValid() {
    for (Episode episode : episodeList) {
      if (!episode.isValid()) {
        return false;
      }
    }
    return true;
  }
}

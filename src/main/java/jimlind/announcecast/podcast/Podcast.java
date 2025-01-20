package jimlind.announcecast.podcast;

import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Podcast {
  private String title;
  private String description;
  private String summary;
  private String feedUrl;
  private String showUrl;
  private String imageUrl;
  private String author;
  private List<Episode> episodeList = new LinkedList<>();

  public void addEpisode(Episode episode) {
    this.episodeList.add(episode);
  }
}

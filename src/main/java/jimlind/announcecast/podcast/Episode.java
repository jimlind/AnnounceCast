package jimlind.announcecast.podcast;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class Episode {
  private @Nullable String title;
  private @Nullable String guid;
  private @Nullable String description;
  private @Nullable String summary;
  private @Nullable String link;
  private @Nullable String imageUrl;
  private @Nullable String thumbnailUrl;
  private @Nullable String seasonId;
  private @Nullable String episodeId;
  private @Nullable String duration;
  private @Nullable String pubDate;
  private @Nullable String explicit;
  private @Nullable String mpegUrl;
  private @Nullable String m4aUrl;

  public boolean isValid() {
    return (title != null && !title.isBlank())
        && (guid != null && !guid.isBlank())
        && (pubDate != null && !pubDate.isBlank());
  }
}

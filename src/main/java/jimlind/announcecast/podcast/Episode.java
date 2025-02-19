package jimlind.announcecast.podcast;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class Episode {
  private String title;
  private String guid;
  private @Nullable String description;
  private String summary;
  private @Nullable String link;
  private @Nullable String imageUrl;
  private @Nullable String thumbnailUrl;
  private @Nullable String seasonId;
  private @Nullable String episodeId;
  private @Nullable String duration;
  private @Nullable String explicit;
  private @Nullable String mpegUrl;
  private @Nullable String m4aUrl;
}

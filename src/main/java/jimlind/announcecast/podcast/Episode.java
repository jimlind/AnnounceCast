package jimlind.announcecast.podcast;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class Episode {
  private String title;
  private String guid;
  private String description;
  private String summary;
  private String link;
  private String imageUrl;
  private @Nullable String seasonId;
  private @Nullable String episodeId;
  private String duration;
  private String explicit;
}

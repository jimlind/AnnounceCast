package jimlind.announcecast.storage.model;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Setter
public class PostedFeed {
  @Getter private String id;
  @Getter private String url;
  @Nullable private String guid;

  public String getGuid() {
    if (this.guid == null || this.guid.isBlank()) {
      return "";
    }

    return this.guid;
  }
}

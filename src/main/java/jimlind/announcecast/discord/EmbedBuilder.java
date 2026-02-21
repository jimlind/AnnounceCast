package jimlind.announcecast.discord;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.Color;

public class EmbedBuilder extends net.dv8tion.jda.api.EmbedBuilder {
  private static final int MAX_TITLE_LENGTH = 256;
  private static final Color DEFAULT_COLOR = new Color(122, 184, 122);

  public EmbedBuilder() {
    super();
    this.setColor(DEFAULT_COLOR);
  }

  @Override
  public net.dv8tion.jda.api.@NonNull EmbedBuilder setTitle(@Nullable String title) {
    return super.setTitle(truncateTitle(title));
  }

  @Override
  public net.dv8tion.jda.api.@NonNull EmbedBuilder setTitle(
      @Nullable String title, @Nullable String url) {
    return super.setTitle(truncateTitle(title), url);
  }

  private @Nullable String truncateTitle(@Nullable String title) {
    if (title == null) {
      return null;
    }

    if (title.length() <= MAX_TITLE_LENGTH) {
      return title;
    }
    return title.substring(0, MAX_TITLE_LENGTH - 3) + "...";
  }
}

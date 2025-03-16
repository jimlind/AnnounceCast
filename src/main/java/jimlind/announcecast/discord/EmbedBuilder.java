package jimlind.announcecast.discord;

import java.awt.*;

public class EmbedBuilder extends net.dv8tion.jda.api.EmbedBuilder {
  private static final Color DEFAULT_COLOR = new Color(122, 184, 122);

  public EmbedBuilder() {
    super();
    this.setColor(DEFAULT_COLOR);
  }
}

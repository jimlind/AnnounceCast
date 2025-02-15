package jimlind.announcecast.integration.context;

import jimlind.announcecast.podcast.Podcast;
import lombok.Getter;

@Getter
public class HelpContext {
  private final String name;
  private final String version;
  private final long podcastCount;
  private final long guildCount;
  private final Podcast podcast;
  private boolean viewChannelEnabled = false;
  private boolean sendMessageEnabled = false;
  private boolean embedLinkEnabled = false;

  public HelpContext(
      String name,
      String version,
      long podcastCount,
      long guildCount,
      boolean viewChannelEnabled,
      boolean sendMessageEnabled,
      boolean embedLinkEnabled,
      Podcast podcast) {
    this.name = name;
    this.version = version;
    this.podcastCount = podcastCount;
    this.guildCount = guildCount;
    this.viewChannelEnabled = viewChannelEnabled;
    this.sendMessageEnabled = sendMessageEnabled;
    this.embedLinkEnabled = embedLinkEnabled;
    this.podcast = podcast;
  }
}

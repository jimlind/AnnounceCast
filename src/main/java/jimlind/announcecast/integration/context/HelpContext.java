package jimlind.announcecast.integration.context;

import com.google.inject.Inject;
import jimlind.announcecast.storage.db.Feed;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Getter
public class HelpContext {
  String name;
  String version;
  long podcastCount;
  long guildCount;

  @Inject private Feed feed;

  public HelpContext build(SlashCommandInteractionEvent event) {
    this.name = getClass().getPackage().getImplementationTitle();
    this.version = getClass().getPackage().getImplementationVersion();
    this.podcastCount = this.feed.getCount();
    this.guildCount =
        event.getJDA().getShardManager() != null
            ? event.getJDA().getShardManager().getGuildCache().size()
            : event.getJDA().getGuildCache().size();

    return this;
  }
}

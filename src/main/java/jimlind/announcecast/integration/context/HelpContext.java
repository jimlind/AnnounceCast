package jimlind.announcecast.integration.context;

import com.google.inject.Inject;
import jimlind.announcecast.storage.db.Feed;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class HelpContext {
  @Getter private String name;
  @Getter private String version;
  @Getter private long podcastCount;
  @Getter private long guildCount;
  @Getter private boolean viewChannelEnabled = false;
  @Getter private boolean sendMessageEnabled = false;
  @Getter private boolean embedLinkEnabled = false;

  @Inject private Feed feed;

  public HelpContext build(SlashCommandInteractionEvent event) {
    this.name = getClass().getPackage().getImplementationTitle();
    this.version = getClass().getPackage().getImplementationVersion();
    this.podcastCount = this.feed.getCount();
    this.guildCount =
        event.getJDA().getShardManager() != null
            ? event.getJDA().getShardManager().getGuildCache().size()
            : event.getJDA().getGuildCache().size();

    // If command was not invoked in a guild exit early
    Guild guild = event.getGuild();
    if (guild == null) {
      return this;
    }

    Member member = guild.getSelfMember();
    GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();

    this.viewChannelEnabled = member.hasPermission(channel, Permission.VIEW_CHANNEL);
    this.sendMessageEnabled =
        channel.getType().isThread()
            ? member.hasPermission(channel, Permission.MESSAGE_SEND_IN_THREADS)
            : member.hasPermission(channel, Permission.MESSAGE_SEND);
    this.embedLinkEnabled = member.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS);

    return this;
  }
}

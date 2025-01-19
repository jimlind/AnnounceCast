package jimlind.announcecast.integration.action;

import com.google.inject.Inject;
import jimlind.announcecast.integration.context.HelpContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.storage.db.Feed;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class HelpAction {
  @Inject private Client client;
  @Inject private Feed feed;

  public HelpContext run(SlashCommandInteractionEvent event) {
    String name = getClass().getPackage().getImplementationTitle();
    String version = getClass().getPackage().getImplementationVersion();
    long podcastCount = this.feed.getCount();
    long guildCount =
        event.getJDA().getShardManager() != null
            ? event.getJDA().getShardManager().getGuildCache().size()
            : event.getJDA().getGuildCache().size();

    boolean viewChannelEnabled = false;
    boolean sendMessageEnabled = false;
    boolean embedLinkEnabled = false;

    // Some of this data only available if event happened in a guild so wrap it
    // But I'm not really sure if this could happen outside a guild so maybe there is something
    // cleaner I can worry about later
    if (event.getGuild() != null) {
      Member member = event.getGuild().getSelfMember();
      GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
      viewChannelEnabled = member.hasPermission(channel, Permission.VIEW_CHANNEL);
      sendMessageEnabled =
          channel.getType().isThread()
              ? member.hasPermission(channel, Permission.MESSAGE_SEND_IN_THREADS)
              : member.hasPermission(channel, Permission.MESSAGE_SEND);
      embedLinkEnabled = member.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS);
    }

    Podcast podcast = null;

    OptionMapping testOption = event.getInteraction().getOption("test");
    if (testOption != null && testOption.getAsBoolean()) {
      String feedUrl = "https://anchor.fm/s/238d77c8/podcast/rss";
      podcast = this.client.createPodcastFromFeedUrl(feedUrl, 1);
    }

    return new HelpContext(
        name,
        version,
        podcastCount,
        guildCount,
        viewChannelEnabled,
        sendMessageEnabled,
        embedLinkEnabled,
        podcast);
  }
}

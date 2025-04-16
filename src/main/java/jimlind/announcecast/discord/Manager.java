package jimlind.announcecast.discord;

import com.google.inject.Inject;
import jimlind.announcecast.Helper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class Manager {
  @Inject private Listeners listeners;
  private @Nullable ShardManager shardManager;

  public void run(String discordBotToken) {
    this.shardManager =
        DefaultShardManagerBuilder.createLight(discordBotToken)
            .addEventListeners(listeners)
            .setActivity(Activity.listening("Slash Commands"))
            .build();
  }

  public void sendMessage(
      String channelId, String message, MessageEmbed embed, Runnable onSuccess) {
    if (this.shardManager == null) {
      return;
    }

    // If we can't locate the channel we should exit early
    GuildMessageChannel channel =
        this.shardManager.getChannelById(GuildMessageChannel.class, channelId);
    if (channel == null) {
      return;
    }

    // Check access to channel viewing requirements
    Member member = channel.getGuild().getSelfMember();
    if (!member.hasPermission(channel, Permission.VIEW_CHANNEL)) {
      return;
    }

    // Check access to thread or channel send message permissions
    if (channel.getType().isThread()
        ? !member.hasPermission(channel, Permission.MESSAGE_SEND_IN_THREADS)
        : !member.hasPermission(channel, Permission.MESSAGE_SEND)) {
      return;
    }

    // Check access to embed link permissions
    if (!member.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
      return;
    }

    try {
      channel
          .sendMessage(message)
          .setEmbeds(embed)
          .queue(
              m -> {
                onSuccess.run();
                sendSuccess(m, embed, channel);
              },
              t -> sendFailure(t, embed, channel));
    } catch (Exception e) {
      log.atWarn().setMessage("Message Send Exception").addKeyValue("exception", e).log();
    }
  }

  public void sendSuccess(Message message, MessageEmbed messageEmbed, GuildMessageChannel channel) {
    log.atInfo()
        .setMessage("Message Send Success")
        .addKeyValue("message", Helper.objectToString(message))
        .addKeyValue("messageEmbed", Helper.objectToString(messageEmbed))
        .addKeyValue("channel", Helper.objectToString(channel))
        .log();
  }

  public void sendFailure(Throwable throwable, MessageEmbed message, GuildMessageChannel channel) {
    log.atInfo()
        .setMessage("Message Send Failure")
        .addKeyValue("throwable", throwable)
        .addKeyValue("message", Helper.objectToString(message))
        .addKeyValue("channel", Helper.objectToString(channel))
        .log();
  }

  public @Nullable GuildChannel getChannel(String channelId) {
    if (this.shardManager == null) {
      return null;
    }
    return this.shardManager.getChannelById(GuildMessageChannel.class, channelId);
  }

  public @Nullable Role getRole(String roleId) {
    if (this.shardManager == null) {
      return null;
    }
    return this.shardManager.getRoleById(roleId);
  }

  public void halt() {
    if (this.shardManager != null) {
      this.shardManager.shutdown();
    }
  }
}

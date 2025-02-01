package jimlind.announcecast.discord;

import com.google.inject.Inject;
import jimlind.announcecast.Helper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
            .build();
  }

  public void sendMessage(String channelId, MessageEmbed message) {
    if (this.shardManager == null) {
      return;
    }
    GuildMessageChannel channel =
        this.shardManager.getChannelById(GuildMessageChannel.class, channelId);
    if (channel == null) {
      return;
    }
    channel
        .sendMessageEmbeds(message)
        .queue(m -> sendSuccess(m, message, channel), m -> sendFailure(message, channel));
  }

  public void sendSuccess(Message message, MessageEmbed messageEmbed, GuildMessageChannel channel) {
    log.atInfo()
        .setMessage("Message Send Success")
        .addKeyValue("message", Helper.objectToString(message))
        .addKeyValue("messageEmbed", Helper.objectToString(messageEmbed))
        .addKeyValue("channel", Helper.objectToString(channel))
        .log();
  }

  public void sendFailure(MessageEmbed message, GuildMessageChannel channel) {
    log.atInfo()
        .setMessage("Message Send Failure")
        .addKeyValue("message", Helper.objectToString(message))
        .addKeyValue("channel", Helper.objectToString(channel))
        .log();
  }

  public void halt() {
    if (this.shardManager != null) {
      this.shardManager.shutdown();
    }
  }
}

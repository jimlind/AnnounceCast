package jimlind.announcecast.discord;

import com.google.inject.Inject;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;

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
    channel.sendMessageEmbeds(message).queue();
  }

  public void halt() {
    if (this.shardManager != null) {
      this.shardManager.shutdown();
    }
  }
}

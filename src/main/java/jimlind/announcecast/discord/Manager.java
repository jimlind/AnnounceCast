package jimlind.announcecast.discord;

import com.google.inject.Inject;
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

  public void halt() {
    if (this.shardManager != null) {
      this.shardManager.shutdown();
    }
  }
}

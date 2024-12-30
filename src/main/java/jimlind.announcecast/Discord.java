package jimlind.announcecast;

import com.google.inject.Inject;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

public class Discord {
  @Inject private Listeners listeners;

  public void run() {
    String token = System.getenv("DISCORD_BOT_TOKEN");
    DefaultShardManagerBuilder.createLight(token).addEventListeners(listeners).build();
  }
}

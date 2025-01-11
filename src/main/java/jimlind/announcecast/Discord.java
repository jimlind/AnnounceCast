package jimlind.announcecast;

import com.google.inject.Inject;
import jimlind.announcecast.discord.Listeners;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

public class Discord {
  @Inject private Listeners listeners;

  public void run(String discordBotToken) {
    DefaultShardManagerBuilder.createLight(discordBotToken).addEventListeners(listeners).build();
  }
}

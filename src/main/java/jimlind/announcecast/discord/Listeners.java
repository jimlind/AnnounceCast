package jimlind.announcecast.discord;

import com.google.inject.Inject;
import jimlind.announcecast.scraper.Schedule;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class Listeners extends ListenerAdapter {
  @Inject private SlashCommand slashCommandManager;
  @Inject private Schedule schedule;

  private int readyCount = 0;

  @Override
  public void onReady(ReadyEvent e) {
    readyCount++;

    log.atInfo()
        .setMessage("Manager client logged in on {} servers")
        .addArgument(e.getJDA().getGuildCache().size())
        .log();

    ShardManager shardManager = e.getJDA().getShardManager();
    if (shardManager != null && readyCount == shardManager.getShardsTotal()) {
      this.schedule.startScrapeQueueRead();
    }
  }

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    boolean processSuccess = this.slashCommandManager.process(event);
    if (processSuccess) {
      log.atInfo()
          .setMessage("Successfully processed slash command.")
          .addKeyValue("event", event.getName())
          .log();
    } else {
      log.atInfo()
          .setMessage("Failed to process slash command.")
          .addKeyValue("event", event.getName())
          .log();
    }
  }
}

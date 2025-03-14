package jimlind.announcecast.discord;

import com.google.inject.Inject;
import jimlind.announcecast.scraper.Schedule;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class Listeners extends ListenerAdapter {
  @Inject private DirectMessage directMessage;
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
    // Ignore slack commands in private messages
    if (event.getChannel().getType() == ChannelType.PRIVATE) {
      this.directMessage.process(event);
      return;
    }

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

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    // Ignore messages in public channels or posted by bots
    if (!event.isFromType(ChannelType.PRIVATE)
        || event.getAuthor().isBot()
        || event.getMessage().getContentRaw().startsWith("/")) {
      return;
    }

    this.directMessage.process(event);
    log.atInfo()
        .setMessage("Successfully processed message.")
        .addKeyValue("message", event.getMessage())
        .log();
  }
}

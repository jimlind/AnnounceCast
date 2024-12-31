package jimlind.announcecast.discord;

import com.google.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Listeners extends ListenerAdapter {
  @Inject private Message message;
  @Inject private SlashCommandManager slashCommandManager;

  @Override
  public void onReady(ReadyEvent e) {
    JDA jda = e.getJDA();
    GuildMessageChannel messageChannel =
        jda.getChannelById(GuildMessageChannel.class, "1203413874183774290");
    if (messageChannel != null) {
      String newMessage = this.message.build();
      // Don't send this message.
      // It is mostly noise, but good to have the example around.
      // messageChannel.sendMessage(newMessage).queue();
    }
  }

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    boolean processSuccess = this.slashCommandManager.process(event);
    if (processSuccess) {
      System.out.println("Slash Success!");
      //          log.atInfo()
      //                  .setMessage("Successfully processed slash command.")
      //                  .addKeyValue("event", event.getName())
      //                  .log();
    } else {
      System.out.println("Slash Failure!");
      //          log.atInfo()
      //                  .setMessage("Failed to process slash command.")
      //                  .addKeyValue("event", event.getName())
      //                  .log();
    }
  }
}

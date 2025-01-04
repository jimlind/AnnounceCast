package jimlind.announcecast.discord;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.discord.message.FollowingMessageList;
import jimlind.announcecast.discord.message.HelpMessageList;
import jimlind.announcecast.discord.message.SearchMessageList;
import jimlind.announcecast.integration.context.FollowingContext;
import jimlind.announcecast.integration.context.HelpContext;
import jimlind.announcecast.integration.context.SearchContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommand {
  @Inject private FollowingContext followingContext;
  @Inject private HelpContext helpContext;
  @Inject private SearchContext searchContext;

  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    List<MessageEmbed> messageList =
        switch (event.getName()) {
          case "search" -> SearchMessageList.build(this.searchContext.build(event));
          case "following" -> FollowingMessageList.build(this.followingContext.build(event));
          default -> HelpMessageList.build(this.helpContext.build(event));
        };

    event.getHook().sendMessageEmbeds(messageList.removeFirst()).queue();
    for (MessageEmbed messageEmbed : messageList) {
      event.getChannel().sendMessageEmbeds(messageEmbed).queue();
    }
    return true;
  }
}

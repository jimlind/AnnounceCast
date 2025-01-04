package jimlind.announcecast.discord;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.discord.message.FollowingMessageList;
import jimlind.announcecast.discord.message.HelpMessageList;
import jimlind.announcecast.discord.message.SearchMessageList;
import jimlind.announcecast.integration.context.FollowingContext;
import jimlind.announcecast.integration.context.HelpContext;
import jimlind.announcecast.integration.context.SearchContext;
import jimlind.announcecast.storage.db.Joined;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommandManager {
  @Inject private FollowingContext followingContext;
  @Inject private HelpContext helpContext;
  @Inject private SearchContext searchContext;
  @Inject private Joined joined;

  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    String eventName = event.getName();

    List<MessageEmbed> messageList = HelpMessageList.build(this.helpContext.build(event));
    if (eventName.equals("search")) {
      messageList = SearchMessageList.build(this.searchContext.build(event));
    } else if (eventName.equals("following")) {
      messageList = FollowingMessageList.build(this.followingContext.build(event));
    }

    event.getHook().sendMessageEmbeds(messageList.removeFirst()).queue();
    for (MessageEmbed messageEmbed : messageList) {
      event.getChannel().sendMessageEmbeds(messageEmbed).queue();
    }
    return true;
  }
}

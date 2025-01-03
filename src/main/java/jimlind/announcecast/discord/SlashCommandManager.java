package jimlind.announcecast.discord;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.discord.message.HelpMessageList;
import jimlind.announcecast.discord.message.SearchMessageList;
import jimlind.announcecast.integration.context.HelpContext;
import jimlind.announcecast.integration.context.SearchContext;
import jimlind.announcecast.storage.db.Joined;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommandManager {
  @Inject private HelpContext helpContext;
  @Inject private SearchContext searchContext;
  @Inject private Joined joined;

  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    String eventName = event.getName();

    if (eventName.equals("search")) {
      List<MessageEmbed> messageList = SearchMessageList.build(this.searchContext.build(event));
      event.getHook().sendMessageEmbeds(messageList).queue();
    } else if (eventName.equals("help")) {
      List<MessageEmbed> messageList = HelpMessageList.build(this.helpContext.build(event));
      event.getHook().sendMessageEmbeds(messageList).queue();
    } else if (eventName.equals("following")) {
      MessageChannelUnion messageChannel = event.getChannel();
      try {
        List<jimlind.announcecast.storage.model.Feed> feedList =
            this.joined.getFeedsByChannelId(messageChannel.getId());
        for (jimlind.announcecast.storage.model.Feed feed1 : feedList) {
          System.out.println(feed1.getTitle());
        }
      } catch (Exception ignored) {
        // Ignore podcast message creation or send errors for now
        System.out.println(ignored);
      }
    }

    return true;
  }
}

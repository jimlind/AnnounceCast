package jimlind.announcecast.discord;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.discord.message.FollowMessageList;
import jimlind.announcecast.discord.message.FollowingMessageList;
import jimlind.announcecast.discord.message.HelpMessageList;
import jimlind.announcecast.discord.message.SearchMessageList;
import jimlind.announcecast.integration.action.FollowAction;
import jimlind.announcecast.integration.context.FollowContext;
import jimlind.announcecast.integration.context.FollowingContext;
import jimlind.announcecast.integration.context.HelpContext;
import jimlind.announcecast.integration.context.SearchContext;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommand {
  @Inject private FollowAction followAction;
  @Inject private FollowContext followContext;
  @Inject private FollowingContext followingContext;
  @Inject private HelpContext helpContext;
  @Inject private SearchContext searchContext;

  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    List<MessageEmbed> messageList =
        switch (event.getName()) {
          case "follow" -> {
            Podcast podcast = this.followAction.run(event);
            yield FollowMessageList.build(this.followContext.build(event, podcast));
          }
          case "following" -> FollowingMessageList.build(this.followingContext.build(event));
          case "search" -> SearchMessageList.build(this.searchContext.build(event));
          default -> HelpMessageList.build(this.helpContext.build(event));
        };

    event.getHook().sendMessageEmbeds(messageList.getFirst()).queue();
    messageList.stream()
        .skip(1)
        .forEach(message -> event.getChannel().sendMessageEmbeds(message).queue());

    return true;
  }
}

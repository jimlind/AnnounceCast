package jimlind.announcecast.discord;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.discord.message.*;
import jimlind.announcecast.integration.action.*;
import jimlind.announcecast.integration.context.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Slf4j
public class SlashCommand {
  @Inject private FollowAction followAction;
  @Inject private FollowingAction followingAction;
  @Inject private FollowRssAction followRssAction;
  @Inject private HelpContext helpContext;
  @Inject private SearchAction searchAction;
  @Inject private UnfollowAction unfollowAction;

  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    List<MessageEmbed> messageList =
        switch (event.getName()) {
          case "follow" -> FollowMessageList.build(this.followAction.run(event));
          case "follow-rss" -> FollowMessageList.build(this.followRssAction.run(event));
          case "following" -> FollowingMessageList.build(this.followingAction.run(event));
          case "search" -> SearchMessageList.build(this.searchAction.run(event));
          case "unfollow" -> UnfollowMessageList.build(this.unfollowAction.run(event));
          default -> HelpMessageList.build(this.helpContext.build(event));
        };

    if (messageList.isEmpty()) {
      log.atWarn().setMessage("Nothing returned in the message list").log();
      return true;
    }

    event.getHook().sendMessageEmbeds(messageList.getFirst()).queue();
    messageList.stream()
        .skip(1)
        .forEach(message -> event.getChannel().sendMessageEmbeds(message).queue());

    return true;
  }
}

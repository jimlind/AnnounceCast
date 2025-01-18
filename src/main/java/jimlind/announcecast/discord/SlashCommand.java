package jimlind.announcecast.discord;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.discord.message.*;
import jimlind.announcecast.integration.action.FollowAction;
import jimlind.announcecast.integration.action.FollowRssAction;
import jimlind.announcecast.integration.action.UnfollowAction;
import jimlind.announcecast.integration.context.*;
import jimlind.announcecast.podcast.Podcast;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommand {
  @Inject private FollowAction followAction;
  @Inject private FollowContext followContext;
  @Inject private FollowRssAction followRssAction;
  @Inject private FollowingContext followingContext;
  @Inject private HelpContext helpContext;
  @Inject private SearchContext searchContext;
  @Inject private UnfollowAction unfollowAction;
  @Inject private UnfollowContext unfollowContext;

  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    List<MessageEmbed> messageList =
        switch (event.getName()) {
          case "follow" -> {
            Podcast podcast = this.followAction.run(event);
            yield FollowMessageList.build(this.followContext.build(event, podcast));
          }
          case "follow-rss" -> {
            Podcast podcast = this.followRssAction.run(event);
            yield FollowMessageList.build(this.followContext.build(event, podcast));
          }
          case "following" -> FollowingMessageList.build(this.followingContext.build(event));
          case "search" -> SearchMessageList.build(this.searchContext.build(event));
          case "unfollow" -> {
            Podcast podcast = this.unfollowAction.run(event);
            yield UnfollowMessageList.build(this.unfollowContext.build(event, podcast));
          }
          default -> HelpMessageList.build(this.helpContext.build(event));
        };

    if (messageList.isEmpty()) {
      // TODO LOG THE ERROR
      System.out.println("Nothing returned in the message list");
      return true;
    }

    event.getHook().sendMessageEmbeds(messageList.getFirst()).queue();
    messageList.stream()
        .skip(1)
        .forEach(message -> event.getChannel().sendMessageEmbeds(message).queue());

    return true;
  }
}

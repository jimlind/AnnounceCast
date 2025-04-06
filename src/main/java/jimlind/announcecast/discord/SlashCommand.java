package jimlind.announcecast.discord;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.discord.message.*;
import jimlind.announcecast.integration.action.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Slf4j
public class SlashCommand {
  @Inject private AdminAction adminAction;
  @Inject private FollowAction followAction;
  @Inject private FollowingAction followingAction;
  @Inject private FollowRssAction followRssAction;
  @Inject private HelpAction helpAction;
  @Inject private Manager manager;
  @Inject private PrioritizeAction prioritizeAction;
  @Inject private SearchAction searchAction;
  @Inject private SettingsAction settingsAction;
  @Inject private TagAction tagAction;
  @Inject private UnfollowAction unfollowAction;

  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    List<MessageEmbed> messageList =
        switch (event.getName()) {
          // Public
          case "follow" -> FollowMessageList.build(this.followAction.run(event));
          case "follow-rss" -> FollowMessageList.build(this.followRssAction.run(event));
          case "following" -> FollowingMessageList.build(this.followingAction.run(event));
          case "search" -> SearchMessageList.build(this.searchAction.run(event));
          case "unfollow" -> UnfollowMessageList.build(this.unfollowAction.run(event));
          // Member Exclusive
          case "settings" -> SettingsMessageList.build(this.settingsAction.run(event));
          case "prioritize" -> SettingsMessageList.build(this.prioritizeAction.run(event));
          case "tag" -> SettingsMessageList.build(this.tagAction.run(event));
          // Help
          default -> HelpMessageList.build(this.helpAction.run(event));
        };

    if (messageList.isEmpty()) {
      log.atWarn().setMessage("Nothing returned in the message list").log();
      return true;
    }

    event.getHook().sendMessageEmbeds(messageList.getFirst()).queue();
    messageList.stream().skip(1).forEach(m -> sendMessage(event, m));
    return true;
  }

  private void sendMessage(SlashCommandInteractionEvent event, MessageEmbed message) {
    this.manager.sendMessage(event.getChannelId(), message, () -> {});
  }
}

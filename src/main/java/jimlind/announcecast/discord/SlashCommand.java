package jimlind.announcecast.discord;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.discord.message.FollowMessageList;
import jimlind.announcecast.discord.message.FollowingMessageList;
import jimlind.announcecast.discord.message.HelpMessageList;
import jimlind.announcecast.discord.message.SearchMessageList;
import jimlind.announcecast.discord.message.SettingsMessageList;
import jimlind.announcecast.discord.message.UnfollowMessageList;
import jimlind.announcecast.integration.action.FollowAction;
import jimlind.announcecast.integration.action.FollowRssAction;
import jimlind.announcecast.integration.action.FollowingAction;
import jimlind.announcecast.integration.action.HelpAction;
import jimlind.announcecast.integration.action.PrioritizeAction;
import jimlind.announcecast.integration.action.SearchAction;
import jimlind.announcecast.integration.action.SettingsAction;
import jimlind.announcecast.integration.action.TagAction;
import jimlind.announcecast.integration.action.UnfollowAction;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Slf4j
@Singleton
public class SlashCommand {
  private final FollowAction followAction;
  private final FollowingAction followingAction;
  private final FollowRssAction followRssAction;
  private final HelpAction helpAction;
  private final Manager manager;
  private final PrioritizeAction prioritizeAction;
  private final SearchAction searchAction;
  private final SettingsAction settingsAction;
  private final TagAction tagAction;
  private final UnfollowAction unfollowAction;

  @Inject
  public SlashCommand(
      FollowAction followAction,
      FollowingAction followingAction,
      FollowRssAction followRssAction,
      HelpAction helpAction,
      Manager manager,
      PrioritizeAction prioritizeAction,
      SearchAction searchAction,
      SettingsAction settingsAction,
      TagAction tagAction,
      UnfollowAction unfollowAction) {
    this.followAction = followAction;
    this.followingAction = followingAction;
    this.followRssAction = followRssAction;
    this.helpAction = helpAction;
    this.manager = manager;
    this.prioritizeAction = prioritizeAction;
    this.searchAction = searchAction;
    this.settingsAction = settingsAction;
    this.tagAction = tagAction;
    this.unfollowAction = unfollowAction;
  }

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

  private void sendMessage(SlashCommandInteractionEvent event, MessageEmbed embed) {
    this.manager.sendMessage(event.getChannelId(), "", embed, () -> {});
  }
}

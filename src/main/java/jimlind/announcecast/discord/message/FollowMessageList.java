package jimlind.announcecast.discord.message;

import java.util.ArrayList;
import java.util.List;
import jimlind.announcecast.integration.context.FollowContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class FollowMessageList {
  public static List<MessageEmbed> build(FollowContext context) {
    List<MessageEmbed> messageList = new ArrayList<>();

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setDescription("You are following a new podcast...");
    messageList.add(embedBuilder.build());

    messageList.add(PodcastMessage.build(context.getPodcast()));
    messageList.addAll(FeedMessageList.build(context.getFeedList()));

    return messageList;
  }
}

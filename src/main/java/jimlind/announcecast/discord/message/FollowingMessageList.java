package jimlind.announcecast.discord.message;

import java.util.ArrayList;
import java.util.List;
import jimlind.announcecast.integration.context.FollowingContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class FollowingMessageList {
  public static List<MessageEmbed> build(FollowingContext context) {
    ArrayList<MessageEmbed> messageList = new ArrayList<>();
    messageList.add(
        new EmbedBuilder().setDescription("Podcasts followed in this channel...").build());
    messageList.addAll(FeedMessageList.build(context.getFeedList()));

    return messageList;
  }
}

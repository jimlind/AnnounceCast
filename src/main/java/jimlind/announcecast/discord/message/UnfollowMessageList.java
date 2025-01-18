package jimlind.announcecast.discord.message;

import java.util.ArrayList;
import java.util.List;
import jimlind.announcecast.integration.context.UnfollowContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class UnfollowMessageList {
  public static List<MessageEmbed> build(UnfollowContext context) {
    List<MessageEmbed> messageList = new ArrayList<>();

    if (context.getPodcast() == null) {
      messageList.add(NoPodcastMessage.build());
      return messageList;
    }

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setDescription("You are no longer following " + context.getPodcast().getTitle());
    embedBuilder.setThumbnail(context.getPodcast().getImageUrl());

    messageList.add(embedBuilder.build());
    messageList.addAll(FeedMessageList.build(context.getFeedList()));

    return messageList;
  }
}

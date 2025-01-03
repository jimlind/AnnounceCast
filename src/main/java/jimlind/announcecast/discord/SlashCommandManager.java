package jimlind.announcecast.discord;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.discord.message.HelpMessage;
import jimlind.announcecast.discord.message.PodcastMessage;
import jimlind.announcecast.integration.context.HelpContext;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.ITunes;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Joined;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SlashCommandManager {
  @Inject private Client client;
  @Inject private Feed feed;
  @Inject private HelpContext helpContext;
  @Inject private ITunes iTunes;
  @Inject private Joined joined;

  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    String eventName = event.getName();
    MessageChannelUnion messageChannel = event.getChannel();

    if (eventName.equals("search")) {
      OptionMapping keywordsOption = event.getInteraction().getOption("keywords");
      String keywords = keywordsOption != null ? keywordsOption.getAsString() : "";

      List<String> feedList = iTunes.search(keywords, 4);
      String message = String.format("Displaying %s podcasts", feedList.size());
      event.getHook().sendMessage(message).queue();

      for (String feed : feedList) {
        try {
          MessageEmbed messageEmbed =
              PodcastMessage.build(client.createPodcastFromFeedUrl(feed, 0));
          messageChannel.sendMessageEmbeds(messageEmbed).queue();
        } catch (Exception ignored) {
          // Ignore podcast message creation or send errors for now
          System.out.println(ignored);
        }
      }
    } else if (eventName.equals("help")) {
      MessageEmbed messageEmbed = HelpMessage.build(this.helpContext.build(event));
      event.getHook().sendMessageEmbeds(messageEmbed).queue();
    } else if (eventName.equals("following")) {
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

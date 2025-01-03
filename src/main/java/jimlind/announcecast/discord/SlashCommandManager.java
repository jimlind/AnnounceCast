package jimlind.announcecast.discord;

import com.google.inject.Inject;
import java.util.List;
import jimlind.announcecast.discord.message.Help;
import jimlind.announcecast.discord.message.Podcast;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.ITunes;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SlashCommandManager {
  @Inject private Client client;
  @Inject private ITunes iTunes;

  public boolean process(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    String name = event.getName();

    if (name.equals("search")) {
      OptionMapping keywordsOption = event.getInteraction().getOption("keywords");
      String keywords = keywordsOption != null ? keywordsOption.getAsString() : "";

      List<String> feedList = iTunes.search(keywords, 4);
      String message = String.format("Displaying %s podcasts", feedList.size());
      event.getHook().sendMessage(message).queue();

      MessageChannelUnion messageChannel = event.getChannel();

      for (String feed : feedList) {
        try {
          MessageEmbed messageEmbed = Podcast.build(client.createPodcastFromFeedUrl(feed, 0));
          messageChannel.sendMessageEmbeds(messageEmbed).queue();
        } catch (Exception ignored) {
          // Ignore podcast message creation or send errors for now
          System.out.println(ignored);
        }
      }
    } else if (name.equals("help")) {
      try {
        MessageEmbed messageEmbed = Help.build();
        event.getHook().sendMessageEmbeds(messageEmbed).queue();
      } catch (Exception ignored) {
        // Ignore podcast message creation or send errors for now
        System.out.println(ignored);
      }
    }

    return true;
  }
}

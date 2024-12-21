package jimlind.announcecast;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listeners extends ListenerAdapter {
    public void onReady(ReadyEvent e) {
        JDA jda = e.getJDA();
        GuildMessageChannel messageChannel = jda.getChannelById(GuildMessageChannel.class, "1203413874183774290");
        if (messageChannel != null) {
            messageChannel.sendMessage("I'm online bojo.").queue();
        }
    }
}

package jimlind.announcecast;

import java.util.EnumSet;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello Main World!");

        String token = System.getenv("DISCORD_BOT_TOKEN");

        Listeners listeners = new Listeners();
        DefaultShardManagerBuilder.createLight(token)
                .addEventListeners(listeners)
                .build();

    }
}

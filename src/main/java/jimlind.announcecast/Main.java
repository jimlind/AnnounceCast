package jimlind.announcecast;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello Main World!");

        Injector injector = Guice.createInjector(new BasicModule());
        Discord discord = injector.getInstance(Discord.class);
        discord.run();
    }
}

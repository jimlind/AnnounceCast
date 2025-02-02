package jimlind.announcecast;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.FileInputStream;
import java.util.Properties;
import jimlind.announcecast.discord.Manager;
import jimlind.announcecast.discord.ShutdownThread;

public class Main {
  public static void main(String[] args) {
    Properties properties = new Properties();
    try (FileInputStream input = new FileInputStream(args[0])) {
      properties.load(input);
    } catch (Exception e) {
      System.out.println("Application expects a formatted properties files as only argument");
      System.exit(-1);
    }

    Injector injector =
        Guice.createInjector(
            new jimlind.announcecast.discord.BasicModule(),
            new jimlind.announcecast.integration.BasicModule(),
            new jimlind.announcecast.podcast.BasicModule(),
            new jimlind.announcecast.scraper.BasicModule(),
            new jimlind.announcecast.storage.BasicModule());

    // Start the Discord connection manager
    injector.getInstance(Manager.class).run(properties.getProperty("DISCORD_BOT_TOKEN"));

    // Register the shutdownThread to the shutdownHook
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));
  }
}

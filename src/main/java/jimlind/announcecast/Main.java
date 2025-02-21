package jimlind.announcecast;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;
import jimlind.announcecast.administration.Command;
import jimlind.announcecast.discord.Manager;
import jimlind.announcecast.discord.ShutdownThread;
import jimlind.announcecast.scraper.Schedule;

public class Main {
  public static void main(String[] args) {
    Injector injector = createDependencyInjector();

    Properties properties = new Properties();
    String argument = Arrays.stream(args).findFirst().orElse("");
    if (argument.equals("admin")) {
      injector.getInstance(Command.class).run(args);
      System.exit(0);
    }

    try (FileInputStream input = new FileInputStream(argument)) {
      properties.load(input);
    } catch (Exception e) {
      System.out.println("Application expects a formatted properties argument or admin command");
      System.exit(-1);
    }

    // Start the Podcast Scrapers
    injector.getInstance(Schedule.class).startScrapeQueueWrite();
    injector.getInstance(Schedule.class).startSubscriberScrapeQueueWrite();

    // Start the Discord connection manager
    injector.getInstance(Manager.class).run(properties.getProperty("DISCORD_BOT_TOKEN"));

    // Register the shutdownThread to the shutdownHook
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));
  }

  private static Injector createDependencyInjector() {
    return Guice.createInjector(
        new jimlind.announcecast.administration.DependencyInjectionModule(),
        new jimlind.announcecast.discord.DependencyInjectionModule(),
        new jimlind.announcecast.integration.DependencyInjectionModule(),
        new jimlind.announcecast.podcast.BasicModule(),
        new jimlind.announcecast.scraper.DependencyInjectionModule(),
        new jimlind.announcecast.storage.BasicModule());
  }
}

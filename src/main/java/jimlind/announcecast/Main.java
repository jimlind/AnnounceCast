package jimlind.announcecast;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
  public static void main(String[] args) {
    Thread.setDefaultUncaughtExceptionHandler(
        (thread, event) -> {
          log.atWarn()
              .setMessage("Uncaught exception in thread '" + thread.getName() + "'")
              .addKeyValue("threadName", thread.getName())
              .addKeyValue("eventMessage", event.getMessage())
              .log();
        });

    ApplicationComponent component = DaggerApplicationComponent.create();

    Properties properties = new Properties();
    String argument = Arrays.stream(args).findFirst().orElse("");
    if (argument.equals("admin")) {
      component.administrationAction().run();
      System.exit(0);
    }

    try (FileInputStream input = new FileInputStream(argument)) {
      properties.load(input);
    } catch (Exception e) {
      System.out.println("Application expects a formatted properties argument or admin command");
      System.exit(-1);
    }

    // Start the Podcast Scrapers
    component.scraperSchedule().startScrapeQueueWrite();
    component.scraperSchedule().startPromotedScrapeQueueWrite();

    // Start the Discord connection manager
    component.manager().run(component.listeners(), properties.getProperty("DISCORD_BOT_TOKEN"));

    // Register the shutdownThread to the shutdownHook
    Runtime.getRuntime().addShutdownHook(component.shutdownThread());

    // Start the Patreon Updaters
    component.patreonSchedule().startMemberUpdate(properties.getProperty("PATREON_ACCESS_TOKEN"));
  }
}

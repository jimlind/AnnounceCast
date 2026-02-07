package jimlind.announcecast;

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component
public interface ApplicationComponent {

  // Listens to Discord events
  jimlind.announcecast.discord.Listeners listeners();

  // Manages the Discord connection
  jimlind.announcecast.discord.Manager manager();

  // Shuts the Discord connection down successfully
  jimlind.announcecast.discord.ShutdownThread shutdownThread();

  // Scrapes the promoted podcast feeds to find most recent podcasts
  jimlind.announcecast.patreon.Schedule patreonSchedule();

  // Special interface for the admin commands
  jimlind.announcecast.administration.Action administrationAction();
}

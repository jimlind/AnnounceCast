package jimlind.announcecast.scraper;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jimlind.announcecast.scraper.task.ScrapeSinglePodcast;
import jimlind.announcecast.scraper.task.ScrapeSinglePodcastFactory;
import jimlind.announcecast.scraper.task.ScrapeSubscribers;

public class BasicModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Queue.class).in(Scopes.SINGLETON);
    bind(Schedule.class).in(Scopes.SINGLETON);
    bind(ScrapeSubscribers.class).in(Scopes.SINGLETON);

    install(
        new FactoryModuleBuilder()
            .implement(ScrapeSinglePodcast.class, ScrapeSinglePodcast.class)
            .build(ScrapeSinglePodcastFactory.class));
  }
}

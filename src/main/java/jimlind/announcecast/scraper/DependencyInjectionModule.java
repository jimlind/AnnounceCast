package jimlind.announcecast.scraper;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jimlind.announcecast.scraper.task.*;

public class DependencyInjectionModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Helper.class).in(Scopes.SINGLETON);
    bind(Queue.class).in(Scopes.SINGLETON);
    bind(Schedule.class).in(Scopes.SINGLETON);

    bind(ReadQueue.class).in(Scopes.SINGLETON);
    bind(ScrapeGeneral.class).in(Scopes.SINGLETON);
    bind(ScrapeSubscribers.class).in(Scopes.SINGLETON);

    install(
        new FactoryModuleBuilder()
            .implement(ScrapeSinglePodcast.class, ScrapeSinglePodcast.class)
            .build(ScrapeSinglePodcastFactory.class));
  }
}

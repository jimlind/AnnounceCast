package jimlind.announcecast.scraper;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class BasicModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Queue.class).in(Scopes.SINGLETON);
    bind(Task.class).in(Scopes.SINGLETON);
  }
}

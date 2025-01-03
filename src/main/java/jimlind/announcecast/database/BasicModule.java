package jimlind.announcecast.database;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class BasicModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Feed.class).in(Scopes.SINGLETON);
  }
}

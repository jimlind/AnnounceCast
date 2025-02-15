package jimlind.announcecast.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.announcecast.storage.db.*;

public class BasicModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Channel.class).in(Scopes.SINGLETON);
    bind(Connection.class).in(Scopes.SINGLETON);
    bind(Feed.class).in(Scopes.SINGLETON);
    bind(Joined.class).in(Scopes.SINGLETON);
    bind(Posted.class).in(Scopes.SINGLETON);
  }
}

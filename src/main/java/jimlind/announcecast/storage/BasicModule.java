package jimlind.announcecast.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.announcecast.storage.db.Channel;
import jimlind.announcecast.storage.db.Feed;
import jimlind.announcecast.storage.db.Joined;

public class BasicModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Channel.class).in(Scopes.SINGLETON);
    bind(Feed.class).in(Scopes.SINGLETON);
    bind(Joined.class).in(Scopes.SINGLETON);
  }
}

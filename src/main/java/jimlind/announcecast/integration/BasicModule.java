package jimlind.announcecast.integration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.announcecast.integration.context.FollowingContext;
import jimlind.announcecast.integration.context.HelpContext;
import jimlind.announcecast.integration.context.SearchContext;

public class BasicModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(FollowingContext.class).in(Scopes.SINGLETON);
    bind(HelpContext.class).in(Scopes.SINGLETON);
    bind(SearchContext.class).in(Scopes.SINGLETON);
  }
}

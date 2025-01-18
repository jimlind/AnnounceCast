package jimlind.announcecast.integration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.announcecast.integration.action.FollowAction;
import jimlind.announcecast.integration.action.FollowRssAction;
import jimlind.announcecast.integration.action.UnfollowAction;
import jimlind.announcecast.integration.context.*;

public class BasicModule extends AbstractModule {

  @Override
  protected void configure() {
    // Actions
    bind(FollowAction.class).in(Scopes.SINGLETON);
    bind(FollowRssAction.class).in(Scopes.SINGLETON);
    bind(UnfollowAction.class).in(Scopes.SINGLETON);
    // Contexts
    bind(FollowContext.class).in(Scopes.SINGLETON);
    bind(FollowingContext.class).in(Scopes.SINGLETON);
    bind(HelpContext.class).in(Scopes.SINGLETON);
    bind(SearchContext.class).in(Scopes.SINGLETON);
    bind(UnfollowContext.class).in(Scopes.SINGLETON);
  }
}

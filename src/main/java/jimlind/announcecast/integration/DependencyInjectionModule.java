package jimlind.announcecast.integration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.announcecast.integration.action.*;

public class DependencyInjectionModule extends AbstractModule {

  @Override
  protected void configure() {
    // Actions
    bind(AdminAction.class).in(Scopes.SINGLETON);
    bind(FollowAction.class).in(Scopes.SINGLETON);
    bind(FollowingAction.class).in(Scopes.SINGLETON);
    bind(FollowRssAction.class).in(Scopes.SINGLETON);
    bind(HelpAction.class).in(Scopes.SINGLETON);
    bind(PrioritizeAction.class).in(Scopes.SINGLETON);
    bind(SearchAction.class).in(Scopes.SINGLETON);
    bind(SettingsAction.class).in(Scopes.SINGLETON);
    bind(TagAction.class).in(Scopes.SINGLETON);
    bind(UnfollowAction.class).in(Scopes.SINGLETON);
  }
}

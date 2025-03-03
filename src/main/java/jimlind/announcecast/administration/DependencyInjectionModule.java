package jimlind.announcecast.administration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.announcecast.administration.run.DeleteUnfollowedFeeds;
import jimlind.announcecast.administration.run.SendAllFeeds;

public class DependencyInjectionModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Action.class).in(Scopes.SINGLETON);
    bind(Helper.class).in(Scopes.SINGLETON);

    bind(DeleteUnfollowedFeeds.class).in(Scopes.SINGLETON);
    bind(SendAllFeeds.class).in(Scopes.SINGLETON);
  }
}

package jimlind.announcecast.administration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.announcecast.administration.run.DeleteUnfollowedFeeds;
import jimlind.announcecast.administration.run.SendAllFeeds;
import jimlind.announcecast.administration.run.subscriber.GetSubscribers;
import jimlind.announcecast.administration.run.subscriber.SetSubscriber;

public class DependencyInjectionModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Action.class).in(Scopes.SINGLETON);
    bind(Helper.class).in(Scopes.SINGLETON);

    bind(DeleteUnfollowedFeeds.class).in(Scopes.SINGLETON);
    bind(SendAllFeeds.class).in(Scopes.SINGLETON);

    bind(jimlind.announcecast.administration.run.subscriber.Action.class).in(Scopes.SINGLETON);
    bind(GetSubscribers.class).in(Scopes.SINGLETON);
    bind(SetSubscriber.class).in(Scopes.SINGLETON);
  }
}

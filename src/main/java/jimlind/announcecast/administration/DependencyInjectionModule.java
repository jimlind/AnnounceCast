package jimlind.announcecast.administration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.announcecast.administration.run.SendAllFeeds;
import jimlind.announcecast.administration.run.maintenance.*;
import jimlind.announcecast.administration.run.subscriber.*;

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

    bind(jimlind.announcecast.administration.run.maintenance.Action.class).in(Scopes.SINGLETON);
    bind(DeleteUnauthorizedChannels.class).in(Scopes.SINGLETON);
    bind(DeleteUnfollowedFeeds.class).in(Scopes.SINGLETON);
    bind(DeleteUselessFeeds.class).in(Scopes.SINGLETON);
    bind(UpdateFeedUrls.class).in(Scopes.SINGLETON);
    bind(UpdateTitles.class).in(Scopes.SINGLETON);
  }
}

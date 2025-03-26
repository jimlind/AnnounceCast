package jimlind.announcecast.patreon;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class DependencyInjectionModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Client.class).in(Scopes.SINGLETON);
    bind(Schedule.class).in(Scopes.SINGLETON);
    bind(UpdateMembers.class).in(Scopes.SINGLETON);
  }
}

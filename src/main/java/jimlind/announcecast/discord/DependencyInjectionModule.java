package jimlind.announcecast.discord;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class DependencyInjectionModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DirectMessage.class).in(Scopes.SINGLETON);
    bind(Listeners.class).in(Scopes.SINGLETON);
    bind(Manager.class).in(Scopes.SINGLETON);
    bind(ShutdownThread.class).in(Scopes.SINGLETON);
    bind(SlashCommand.class).in(Scopes.SINGLETON);
  }
}

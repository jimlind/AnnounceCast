package jimlind.announcecast.discord;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class BasicModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Listeners.class).in(Scopes.SINGLETON);
    bind(Message.class).in(Scopes.SINGLETON);
    bind(ShutdownThread.class).in(Scopes.SINGLETON);
    bind(SlashCommand.class).in(Scopes.SINGLETON);
  }
}

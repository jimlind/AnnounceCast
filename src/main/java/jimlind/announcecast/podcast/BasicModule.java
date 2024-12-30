package jimlind.announcecast.podcast;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class BasicModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Client.class).in(Scopes.SINGLETON);
    bind(Parser.class).in(Scopes.SINGLETON);
  }
}

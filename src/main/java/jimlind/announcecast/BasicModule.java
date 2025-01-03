package jimlind.announcecast;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class BasicModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Discord.class).in(Scopes.SINGLETON);
  }
}

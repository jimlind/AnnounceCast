package jimlind.announcecast.administration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class DependencyInjectionModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Action.class).in(Scopes.SINGLETON);
    bind(Helper.class).in(Scopes.SINGLETON);
  }
}

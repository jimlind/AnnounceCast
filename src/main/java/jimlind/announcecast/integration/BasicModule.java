package jimlind.announcecast.integration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.announcecast.integration.context.HelpContext;

public class BasicModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(HelpContext.class).in(Scopes.SINGLETON);
  }
}

package jimlind.announcecast.discord;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;

public final class ContainerBuilder {

  private static final Color DEFAULT_COLOR = new Color(122, 184, 122);
  private final List<ContainerChildComponent> components = new ArrayList<>();

  @Inject
  public ContainerBuilder() {
  }

  public ContainerBuilder add(ContainerChildComponent component) {
    components.add(component);
    return this;
  }

  public Container build() {
    return Container.of(components).withAccentColor(DEFAULT_COLOR);
  }
}

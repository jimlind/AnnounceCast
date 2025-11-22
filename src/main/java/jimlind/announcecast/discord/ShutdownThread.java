package jimlind.announcecast.discord;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ShutdownThread extends Thread {
  private final Manager manager;

  @Inject
  public ShutdownThread(Manager manager) {
    this.manager = manager;
  }

  public void run() {
    log.atInfo().setMessage("Shutting things down").log();
    this.manager.halt();
  }
}

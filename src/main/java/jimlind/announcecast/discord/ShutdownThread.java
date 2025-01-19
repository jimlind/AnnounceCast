package jimlind.announcecast.discord;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutdownThread extends Thread {
  @Inject private Manager manager;

  public void run() {
    log.atInfo().setMessage("Shutting things down").log();
    this.manager.halt();
  }
}

package jimlind.announcecast.discord;

import com.google.inject.Inject;
import jimlind.announcecast.Discord;

public class ShutdownThread extends Thread {
  @Inject private Discord discord;

  public void run() {
    // TODO: Log!
    System.out.println("Shutting Things Down!");
    this.discord.halt();
  }
}

package jimlind.announcecast.scraper;

import java.util.LinkedList;

public class Queue {
  private final LinkedList<String> urlList = new LinkedList<>();

  public void set(String url) {
    this.urlList.add(url);
  }

  public String get() {
    // Get the first message from the queue
    // Checking length doesn't seem to be a foolproof way to resolve this so wrapping in a try/catch
    try {
      return this.urlList.pop();
    } catch (Exception e) {
      return null;
    }
  }
}

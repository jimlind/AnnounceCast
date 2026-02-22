package jimlind.announcecast.scraper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;

/**
 * This maintains a podcast queue. When the scraper finds a new episode of a podcast it is added to
 * the podcast queue. Items are removed from the queue when work with them has completed.
 */
@Singleton
public class PodcastQueue {

  private final LinkedList<String> podcastUrlList = new LinkedList<>();

  @Inject
  public PodcastQueue() {}

  public String getPodcast() {
    // Get the first message from the queue
    // Checking length doesn't seem to be a foolproof way to resolve this so wrapping in a try/catch
    try {
      return podcastUrlList.pop();
    } catch (Exception e) {
      return null;
    }
  }

  public void setPodcast(String url) {
    podcastUrlList.add(url);
  }
}

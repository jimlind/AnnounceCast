package jimlind.announcecast.scraper.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.TimerTask;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Podcast;
import jimlind.announcecast.scraper.Queue;

public class ScrapeSinglePodcast extends TimerTask {
  private final Client client;
  private final Queue queue;

  private final String url;

  @Inject
  public ScrapeSinglePodcast(Client client, Queue queue, @Assisted String url) {
    this.client = client;
    this.queue = queue;
    this.url = url;
  }

  @Override
  public void run() {
    Podcast podcast = this.client.createPodcastFromFeedUrl(this.url, 1);
    if (podcast == null) {
      return;
    }

    // This is only for example
    // We actually need to check if there is a new episode
    this.queue.setPodcast(this.url);
  }
}

package jimlind.announcecast.scraper.task;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.storage.db.Feed;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ScrapeGeneral implements Runnable {
  private static final int PAGINATION_SIZE = 20;
  private final Feed feed;
  private final ScrapeSinglePodcastFactory scrapeFactory;
  private int paginationIndex = 0;

  @Inject
  public ScrapeGeneral(Feed feed, ScrapeSinglePodcastFactory scrapeFactory) {
    this.feed = feed;
    this.scrapeFactory = scrapeFactory;
  }

  @Override
  public void run() {
    List<String> urlList = feed.getFeedUrlPage(PAGINATION_SIZE, paginationIndex);
    if (urlList.isEmpty()) {
      paginationIndex = 0;
      return;
    }
    paginationIndex++;
    urlList.forEach(url -> scrapeFactory.create(url).run());
  }
}

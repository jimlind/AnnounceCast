package jimlind.announcecast.scraper.task;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.storage.db.Joined;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ScrapePromoted implements Runnable {
  private final Joined joined;
  private final ScrapeSinglePodcastFactory scrapeFactory;

  @Inject
  public ScrapePromoted(Joined joined, ScrapeSinglePodcastFactory scrapeSinglePodcastFactory) {
    this.joined = joined;
    this.scrapeFactory = scrapeSinglePodcastFactory;
  }

  @Override
  public void run() {
    List<String> urlList = joined.getPromotedFeedUrlList();
    Collections.shuffle(urlList);
    urlList.forEach(url -> scrapeFactory.create(url).run());
  }
}

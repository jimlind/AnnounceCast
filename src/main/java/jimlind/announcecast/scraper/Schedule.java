package jimlind.announcecast.scraper;

import com.google.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import jimlind.announcecast.scraper.task.ReadQueue;
import jimlind.announcecast.scraper.task.ScrapeGeneral;
import jimlind.announcecast.scraper.task.ScrapePromoted;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Schedule {
  public static final long SINGLE_PODCAST_PERIOD = TimeUnit.MINUTES.toMillis(1);
  public static final long PROMOTED_SCRAPE_PERIOD = TimeUnit.HOURS.toMillis(2);
  private static final long GENERAL_SCRAPE_PERIOD = TimeUnit.SECONDS.toMillis(1);
  private static final long QUEUE_READ_DELAY = TimeUnit.SECONDS.toMillis(1);
  private static final long QUEUE_READ_PERIOD = TimeUnit.MILLISECONDS.toMillis(20);

  @Inject private ReadQueue readQueue;
  @Inject private ScrapeGeneral scrapeGeneral;
  @Inject private ScrapePromoted scrapePromoted;

  public void startScrapeQueueWrite() {
    new Timer().schedule(scrapeGeneral, 0, GENERAL_SCRAPE_PERIOD);
  }

  public void startPromotedScrapeQueueWrite() {
    new Timer().schedule(scrapePromoted, 0, PROMOTED_SCRAPE_PERIOD);
  }

  public void startScrapeQueueRead() {
    new Timer().schedule(readQueue, QUEUE_READ_DELAY, QUEUE_READ_PERIOD);
  }
}

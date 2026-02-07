package jimlind.announcecast.scraper;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.core.Scheduler;
import jimlind.announcecast.scraper.task.ReadQueue;
import jimlind.announcecast.scraper.task.ScrapeGeneral;
import jimlind.announcecast.scraper.task.ScrapePromoted;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class Schedule {
  private static final long GENERAL_SCRAPE_PERIOD = 20;
  private static final long PROMOTED_SCRAPE_PERIOD = 60;
  private static final long QUEUE_READ_PERIOD = 5;

  private final ReadQueue readQueue;
  private final ScrapeGeneral scrapeGeneral;
  private final ScrapePromoted scrapePromoted;

  @Inject
  public Schedule(ReadQueue readQueue, ScrapeGeneral scrapeGeneral, ScrapePromoted scrapePromoted) {
    this.readQueue = readQueue;
    this.scrapeGeneral = scrapeGeneral;
    this.scrapePromoted = scrapePromoted;
  }

  public void start() {
    Scheduler scheduler = new Scheduler();
    scheduler.addTask("ScrapeGeneral", scrapeGeneral, GENERAL_SCRAPE_PERIOD);
    scheduler.addTask("ScrapePromoted", scrapePromoted, PROMOTED_SCRAPE_PERIOD);
    scheduler.addTask("ReadQueue", readQueue, QUEUE_READ_PERIOD);
    scheduler.start();
  }
}

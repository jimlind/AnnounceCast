package jimlind.announcecast.scraper;

import jimlind.announcecast.core.taskScheduling.Scheduler;
import jimlind.announcecast.scraper.task.ReadQueue;
import jimlind.announcecast.scraper.task.ScrapeGeneral;
import jimlind.announcecast.scraper.task.ScrapePromoted;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class Schedule {
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
    scheduler.addTask(scrapeGeneral);
    scheduler.addTask(scrapePromoted);
    scheduler.addTask(readQueue);
    scheduler.startAll();
  }
}

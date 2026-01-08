package jimlind.announcecast.scraper.task;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.scraper.Schedule;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.Feed;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ScrapePromoted extends TimerTask {
  private final LinkedList<Timer> promotedTimerList = new LinkedList<>();
  private final Joined joined;
  private final ScrapeSinglePodcastFactory scrapeSinglePodcast;

  @Inject
  public ScrapePromoted(Joined joined, ScrapeSinglePodcastFactory scrapeSinglePodcast) {
    this.joined = joined;
    this.scrapeSinglePodcast = scrapeSinglePodcast;
  }

  @Override
  public void run() {
    try {
      executeScrape();
    } catch (Throwable event) {
      log.atError()
          .setMessage("Error running ScrapeGeneral task")
          .addKeyValue("exception", event.getMessage())
          .log();
    }
  }

  private void executeScrape() {
    while (!promotedTimerList.isEmpty()) {
      promotedTimerList.pop().cancel();
    }

    List<Feed> feedList = joined.getPromotedPodcasts();
    for (Feed feed : feedList) {
      Timer timer = new Timer();
      timer.schedule(scrapeSinglePodcast.create(feed.getUrl()), 0, Schedule.SINGLE_PODCAST_PERIOD);
      promotedTimerList.push(timer);
    }
  }
}

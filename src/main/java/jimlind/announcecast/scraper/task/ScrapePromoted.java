package jimlind.announcecast.scraper.task;

import io.github.jimlind.amaranth.exception.TaskException;
import io.github.jimlind.amaranth.task.FixedRateTask;
import jimlind.announcecast.storage.db.Joined;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

@Slf4j
@Singleton
public class ScrapePromoted extends FixedRateTask {
  private static final long INITIAL_DELAY_MILLIS = 0;
  private static final long PERIOD_MILLIS = 60000; // 1 minute

  private final Joined joined;
  private final ScrapeSinglePodcastFactory scrapeFactory;
  private final TaskExceptionHandler taskExceptionHandler;

  @Inject
  public ScrapePromoted(
      Joined joined,
      ScrapeSinglePodcastFactory scrapeSinglePodcastFactory,
      TaskExceptionHandler taskExceptionHandler) {
    super(INITIAL_DELAY_MILLIS, PERIOD_MILLIS, PERIOD_MILLIS);
    this.joined = joined;
    this.scrapeFactory = scrapeSinglePodcastFactory;
    this.taskExceptionHandler = taskExceptionHandler;
  }

  @Override
  public void runTask() {
    List<String> urlList = joined.getPromotedFeedUrlList();
    Collections.shuffle(urlList);
    urlList.forEach(url -> scrapeFactory.create(url).run());
  }

  @Override
  protected void exceptionConsumer(TaskException exception) {
    taskExceptionHandler.handle(exception);
  }
}

package jimlind.announcecast.scraper.task;

import jimlind.amaranth.exception.TaskException;
import jimlind.amaranth.task.FixedDelayTask;
import jimlind.announcecast.storage.db.Joined;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

@Slf4j
@Singleton
public class ScrapeGeneral extends FixedDelayTask {
  private static final long INITIAL_DELAY_MILLIS = 0;
  private static final long SUBSEQUENT_DELAY_MILLIS = 5000; // 5 seconds
  private static final long TIMEOUT_MILLIS = 60000; // 1 minute
  private static final int PAGINATION_SIZE = 20;

  private final Joined joined;
  private final ScrapeSinglePodcastFactory scrapeFactory;
  private final TaskExceptionHandler taskExceptionHandler;
  private int paginationIndex = 0;

  @Inject
  public ScrapeGeneral(
      Joined joined,
      ScrapeSinglePodcastFactory scrapeFactory,
      TaskExceptionHandler taskExceptionHandler) {
    super(INITIAL_DELAY_MILLIS, SUBSEQUENT_DELAY_MILLIS, TIMEOUT_MILLIS);
    this.joined = joined;
    this.scrapeFactory = scrapeFactory;
    this.taskExceptionHandler = taskExceptionHandler;
  }

  @Override
  public void runTask() {
    List<String> urlList = joined.getFollowedFeedUrlPage(PAGINATION_SIZE, paginationIndex);
    Collections.shuffle(urlList);

    if (urlList.isEmpty()) {
      paginationIndex = 0;
      return;
    }
    paginationIndex++;
    urlList.forEach(url -> scrapeFactory.create(url).run());
  }

  @Override
  protected void exceptionConsumer(TaskException exception) {
    taskExceptionHandler.handle(exception);
  }
}

package jimlind.announcecast.scraper.task;

import com.google.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import jimlind.announcecast.scraper.Schedule;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.Feed;

public class ScrapeSubscribers extends TimerTask {
  private final LinkedList<Timer> subscriberTimerList = new LinkedList<>();
  @Inject private Joined joined;
  @Inject private ScrapeSinglePodcastFactory scrapeSinglePodcast;

  @Override
  public void run() {
    while (!subscriberTimerList.isEmpty()) {
      subscriberTimerList.pop().cancel();
    }

    List<Feed> feedList = joined.getPodcastsSubscribed();
    for (Feed feed : feedList) {
      Timer timer = new Timer();
      timer.schedule(scrapeSinglePodcast.create(feed.getUrl()), 0, Schedule.SINGLE_PODCAST_PERIOD);
      subscriberTimerList.push(timer);
    }
  }
}

package jimlind.announcecast.scraper.task;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import jimlind.announcecast.scraper.DependencyInjectionModule;
import jimlind.announcecast.scraper.Schedule;
import jimlind.announcecast.storage.db.Joined;
import jimlind.announcecast.storage.model.Feed;

public class ScrapeSubscribers extends TimerTask {
  private final LinkedList<Timer> subscriberTimerList = new LinkedList<>();
  @Inject private Joined joined;

  @Override
  public void run() {

    Injector injector = Guice.createInjector(new DependencyInjectionModule());
    ScrapeSinglePodcastFactory factory = injector.getInstance(ScrapeSinglePodcastFactory.class);

    for (Timer timer : subscriberTimerList) {
      timer.cancel();
      // TODO: actually pop them from the list so they can get get picked up by garbage collection
    }

    List<Feed> feedList = joined.getPodcastsSubscribed();
    for (Feed feed : feedList) {
      System.out.println(feed.getTitle());
      Timer timer = new Timer();
      timer.schedule(factory.create(feed.getUrl()), 0, Schedule.SINGLE_PODCAST_PERIOD);
      subscriberTimerList.push(timer);
    }
  }
}

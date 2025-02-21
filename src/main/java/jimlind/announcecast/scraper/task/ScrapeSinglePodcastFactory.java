package jimlind.announcecast.scraper.task;

import com.google.inject.assistedinject.Assisted;

public interface ScrapeSinglePodcastFactory {
  ScrapeSinglePodcast create(@Assisted String url);
}

package jimlind.announcecast.scraper.task;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface ScrapeSinglePodcastFactory {
  ScrapeSinglePodcast create(@Assisted String url);
}

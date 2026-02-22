package jimlind.announcecast.scraper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.MessageDigest;
import java.util.ArrayList;

/**
 * This maintains an episode cache. When the new episode begins the write process it is added to the
 * episode cache. Episodes are removed from the queues when work with them has completed.
 */
@Singleton
public class EpisodeCache {
  private static final int MAX_EPISODE_LIST_SIZE = 1000;
  private final ArrayList<String> episodeHashList = new ArrayList<>();

  @Inject
  public EpisodeCache() {}

  public boolean isEpisodeCached(String feedId, String episodeGuid) {
    return episodeHashList.contains(hashString(feedId + episodeGuid));
  }

  public void setEpisode(String feedId, String episodeGuid) {
    episodeHashList.add(hashString(feedId + episodeGuid));
    trimEpisodeList();
  }

  public void removeEpisode(String feedId, String episodeGuid) {
    episodeHashList.remove(hashString(feedId + episodeGuid));
    trimEpisodeList();
  }

  private void trimEpisodeList() {
    if (episodeHashList.size() > MAX_EPISODE_LIST_SIZE) {
      episodeHashList.subList(0, episodeHashList.size() - MAX_EPISODE_LIST_SIZE).clear();
    }
    episodeHashList.trimToSize();
  }

  private String hashString(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      byte[] hashBytes = digest.digest(input.getBytes());

      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (Exception ignore) {
      // Ignore exceptions
    }
    // If failure return input string
    return input;
  }
}

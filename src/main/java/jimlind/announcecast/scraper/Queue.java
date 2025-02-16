package jimlind.announcecast.scraper;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedList;

public class Queue {
  private final int MAX_EPISODE_LIST_SIZE = 1000;

  private final LinkedList<String> podcastUrlList = new LinkedList<>();
  private final ArrayList<String> episodeHashList = new ArrayList<>();

  public String getPodcast() {
    // Get the first message from the queue
    // Checking length doesn't seem to be a foolproof way to resolve this so wrapping in a try/catch
    try {
      return this.podcastUrlList.pop();
    } catch (Exception e) {
      return null;
    }
  }

  public void setPodcast(String url) {
    this.podcastUrlList.add(url);
  }

  public boolean isEpisodeQueued(String feedId, String episodeGuid) {
    return this.episodeHashList.contains(hashString(feedId + episodeGuid));
  }

  public void setEpisode(String feedId, String episodeGuid) {
    this.episodeHashList.add(hashString(feedId + episodeGuid));
  }

  public void removeEpisode(String feedId, String episodeGuid) {
    this.episodeHashList.remove(hashString(feedId + episodeGuid));
    if (this.episodeHashList.size() > MAX_EPISODE_LIST_SIZE) {
      this.episodeHashList.subList(0, this.episodeHashList.size() - MAX_EPISODE_LIST_SIZE).clear();
    }
    this.episodeHashList.trimToSize();
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

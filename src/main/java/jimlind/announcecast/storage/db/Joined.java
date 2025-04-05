package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import jimlind.announcecast.storage.model.Feed;
import jimlind.announcecast.storage.model.PostedFeed;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class Joined {
  private @Inject jimlind.announcecast.storage.db.Connection connection;

  public List<Feed> getFeedsByChannelId(String channelId) {
    List<Feed> feedList = new ArrayList<>();

    String sql =
        "SELECT id, title FROM feeds INNER JOIN channels ON feeds.id = channels.feed_id WHERE channel_id = ? ORDER BY title";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, channelId);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        Feed feed = new Feed();
        feed.setId(resultSet.getString("id"));
        feed.setTitle(resultSet.getString("title"));
        feedList.add(feed);
      }
    } catch (Exception ignore) {
      log.atWarn()
          .setMessage("Unable to get feeds by channel id")
          .addKeyValue("channelId", channelId)
          .log();
    }

    return feedList;
  }

  public @Nullable PostedFeed getPostedFeedByUrl(String url) {
    String sql =
        """
        SELECT id, url, guid
        FROM feeds
        LEFT JOIN posted ON feeds.id = posted.feed_id
        WHERE url = ?
        """;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, url);
      ResultSet resultSet = statement.executeQuery();
      resultSet.next();

      PostedFeed postedFeed = new PostedFeed();
      postedFeed.setId(resultSet.getString("id"));
      postedFeed.setUrl(resultSet.getString("url"));
      postedFeed.setGuid(resultSet.getString("guid"));

      return postedFeed;
    } catch (Exception ignore) {
      return null;
    }
  }

  public List<PostedFeed> getPaginatedPostedFeed(int paginationSize, int paginationIndex) {
    List<PostedFeed> postedFeedList = new ArrayList<>();

    String selectSql =
        """
        SELECT DISTINCT feeds.id as id, feeds.url, feeds.title, posted.guid
        FROM feeds
        LEFT JOIN posted ON feeds.id = posted.feed_id
        INNER JOIN promoted_feed ON feeds.id = promoted_feed.feed_id
        INNER JOIN channels ON feeds.id = channels.feed_id
        LIMIT ? OFFSET ?;
        """;
    try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
      statement.setInt(1, paginationSize);
      statement.setInt(2, paginationIndex * paginationSize);
      ResultSet resultSet = statement.executeQuery();

      if (!resultSet.isBeforeFirst()) {
        return null;
      }

      while (resultSet.next()) {
        PostedFeed postedFeed = new PostedFeed();
        postedFeed.setId(resultSet.getString("id"));
        postedFeed.setUrl(resultSet.getString("url"));
        postedFeed.setGuid(resultSet.getString("guid"));

        postedFeedList.add(postedFeed);
      }
    } catch (Exception ignore) {
      log.atWarn()
          .setMessage("Unable to get posted feed page")
          .addKeyValue("paginationSize", paginationSize)
          .addKeyValue("paginationIndex", paginationIndex)
          .log();
    }

    return postedFeedList;
  }

  public List<Feed> getFeedsWithoutChannels() {
    List<Feed> feedList = new ArrayList<>();
    String sql =
        """
        SELECT f.id, f.url, f.title
        FROM feeds AS f
        LEFT JOIN channels AS c ON f.id = c.feed_id
        WHERE c.feed_id IS NULL;
        """;
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery(sql);
      while (resultSet.next()) {
        Feed feed = new Feed();
        feed.setId(resultSet.getString("id"));
        feed.setUrl(resultSet.getString("url"));
        feed.setTitle(resultSet.getString("title"));
        feedList.add(feed);
      }
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to get feeds without channels").log();
    }
    return feedList;
  }

  public List<Feed> getPromotedPodcasts() {
    List<Feed> feedList = new ArrayList<>();

    String sql =
        """
        SELECT id, url, title
        FROM feeds
        INNER JOIN promoted_feed ON feeds.id = promoted_feed.feed_id
        LEFT JOIN patreon ON promoted_feed.user_id = patreon.user_id
        WHERE promoted_feed.user_id = 'OVERRIDE'
        OR patreon.user_id IS NOT NULL;
        """;
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery(sql);
      while (resultSet.next()) {
        Feed feed = new Feed();
        feed.setId(resultSet.getString("id"));
        feed.setUrl(resultSet.getString("url"));
        feed.setTitle(resultSet.getString("title"));
        feedList.add(feed);
      }
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to get promoted podcasts").log();
    }

    return feedList;
  }
}

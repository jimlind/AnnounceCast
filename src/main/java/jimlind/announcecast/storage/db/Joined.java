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
        "SELECT id, url, guid FROM feeds INNER JOIN posted ON feeds.id = posted.feed_id WHERE url = ?";
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
        SELECT id, url, guid
        FROM feeds
        INNER JOIN posted ON feeds.id = posted.feed_id
        WHERE EXISTS (SELECT 1 FROM channels WHERE channels.feed_id = feeds.id)
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
}

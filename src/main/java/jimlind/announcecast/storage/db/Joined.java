package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import jimlind.announcecast.storage.model.Feed;

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
      // TODO: Log the exception
    }

    return feedList;
  }
}

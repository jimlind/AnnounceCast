package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.*;

public class Channel {
  private @Inject jimlind.announcecast.storage.db.Connection connection;

  public long getCount() {
    int countValue = 0;
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM channels");
      resultSet.next();
      countValue = resultSet.getInt(1);
    } catch (Exception ignore) {
      // TODO: Log the exception
    }
    return countValue;
  }

  public void addChannel(String feedUrl, String channelId) {
    String insertSql = "INSERT OR IGNORE INTO channels (feed_id, channel_id) VALUES (?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
      statement.setString(1, feedUrl);
      statement.setString(2, channelId);
      statement.executeUpdate();
    } catch (Exception ignore) {
      // TODO: Log the exception
    }
  }

  public void removeChannel(String feedUrl, String channelId) {
    String deleteSql = "DELETE FROM channels WHERE feed_id = ? AND channel_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
      statement.setString(1, feedUrl);
      statement.setString(2, channelId);
      statement.executeUpdate();
    } catch (Exception ignore) {
      // TODO: Log the exception
    }
  }
}

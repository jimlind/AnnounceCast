package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Channel {
  private @Inject jimlind.announcecast.storage.db.Connection connection;

  public long getCount() {
    int countValue = 0;
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM channels");
      resultSet.next();
      countValue = resultSet.getInt(1);
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to get the channel count").log();
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
      log.atWarn()
          .setMessage("Unable to add a new channel")
          .addKeyValue("feedUrl", feedUrl)
          .addKeyValue("channelId", channelId)
          .log();
    }
  }

  public void removeChannel(String feedId, String channelId) {
    String deleteSql = "DELETE FROM channels WHERE feed_id = ? AND channel_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
      statement.setString(1, feedId);
      statement.setString(2, channelId);
      statement.executeUpdate();
    } catch (Exception ignore) {
      log.atWarn()
          .setMessage("Unable to remove a channel")
          .addKeyValue("feedId", feedId)
          .addKeyValue("channelId", channelId)
          .log();
    }
  }

  public List<String> getChannelsByFeedId(String feedId) {
    List<String> results = new ArrayList<>();

    String sql = "SELECT channel_id as id FROM channels WHERE feed_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, feedId);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        results.add(resultSet.getString("id"));
      }
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to load a channel list").addKeyValue("feedId", feedId).log();
    }
    return results;
  }
}

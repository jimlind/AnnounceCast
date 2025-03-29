package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PromotedFeed {
  private @Inject Connection connection;

  public boolean getActiveByFeed(String feedId) {
    boolean result = false;
    String selectSql = "SELECT active FROM promoted_feed WHERE feed_id = ? LIMIT 1";
    try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
      statement.setString(1, feedId);
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        result = resultSet.getInt("active") == 1;
      }
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to get active").addKeyValue("feedId", feedId).log();
    }
    return result;
  }

  public void setActiveByFeed(String feedId, boolean active) {
    String upsertSql =
        """
        INSERT INTO promoted_feed(feed_id, active) VALUES(?, ?)
        ON CONFLICT(feed_id) DO UPDATE
        SET active=? WHERE feed_id = ?
        """;
    try (PreparedStatement statement = connection.prepareStatement(upsertSql)) {
      statement.setString(1, feedId);
      statement.setInt(2, active ? 1 : 0);
      statement.setInt(3, active ? 1 : 0);
      statement.setString(4, feedId);
      statement.execute();
    } catch (Exception exception) {
      log.atWarn()
          .setMessage("Unable to set active")
          .addKeyValue("feedId", feedId)
          .addKeyValue("active", active)
          .addKeyValue("exception", exception)
          .log();
    }
  }

  public void deletePromotedFeedByFeedId(String feedId) {
    String sql = "DELETE FROM promoted_feed WHERE feed_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, feedId);
      statement.executeUpdate();
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to delete promoted_feed").addKeyValue("feedId", feedId).log();
    }
  }
}

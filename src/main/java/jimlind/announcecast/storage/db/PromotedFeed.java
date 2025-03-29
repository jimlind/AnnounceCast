package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PromotedFeed {
  private @Inject Connection connection;

  public boolean promotedFeedExists(String feedId) {
    String sql = "SELECT COUNT(user_id) as value FROM promoted_feed WHERE feed_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, feedId);
      ResultSet resultSet = statement.executeQuery();
      resultSet.next();
      int count = resultSet.getInt("value");
      return count > 0;
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to check promoted feed id existence").log();
    }
    return false;
  }

  public void addPromotedFeed(String feedId, String userId) {
    String sql = "INSERT INTO promoted_feed (feed_id, user_id) VALUES (?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, feedId);
      statement.setString(2, userId);
      statement.execute();
    } catch (Exception exception) {
      System.out.println(exception);
      log.atWarn()
          .setMessage("Unable to set promoted feed")
          .addKeyValue("feedId", feedId)
          .addKeyValue("userId", userId)
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

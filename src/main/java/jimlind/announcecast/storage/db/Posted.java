package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Posted {
  private @Inject jimlind.announcecast.storage.db.Connection connection;

  public String getGuidByFeedId(String feedId) {
    String guid = "";
    String selectSql = "SELECT guid FROM posted WHERE feed_id = ? LIMIT 1";
    try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
      statement.setString(1, feedId);
      ResultSet resultSet = statement.executeQuery();
      resultSet.next();
      guid = resultSet.getString("guid");
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to get feed guid").addKeyValue("feedId", feedId).log();
    }
    return guid;
  }

  public void setGuidByFeed(String feedId, String guid) {
    String upsertSql =
        "INSERT INTO posted(feed_id, guid) VALUES(?, ?) ON CONFLICT(feed_id) DO UPDATE SET guid=? WHERE feed_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(upsertSql)) {
      statement.setString(1, feedId);
      statement.setString(2, guid);
      statement.setString(3, guid);
      statement.setString(4, feedId);
      statement.execute();
    } catch (Exception exception) {
      log.atWarn()
          .setMessage("Unable to set guid")
          .addKeyValue("feedId", feedId)
          .addKeyValue("guid", guid)
          .addKeyValue("exception", exception)
          .log();
    }
  }

  public void deletePostedByFeedId(String feedId) {
    String deleteSql = "DELETE FROM posted WHERE feed_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
      statement.setString(1, feedId);
      statement.executeUpdate();
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to delete posted").addKeyValue("feedId", feedId).log();
    }
  }
}

package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.PreparedStatement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Subscriber {
  private @Inject Connection connection;

  public void setActiveByFeed(String feedId, boolean subscribed) {
    String upsertSql =
        """
        INSERT INTO subscriber(feed_id, active) VALUES(?, ?)
        ON CONFLICT(feed_id) DO UPDATE
        SET active=? WHERE feed_id = ?
        """;
    try (PreparedStatement statement = connection.prepareStatement(upsertSql)) {
      statement.setString(1, feedId);
      statement.setInt(2, subscribed ? 1 : 0);
      statement.setInt(3, subscribed ? 1 : 0);
      statement.setString(4, feedId);
      statement.execute();
    } catch (Exception exception) {
      log.atWarn()
          .setMessage("Unable to set active")
          .addKeyValue("feedId", feedId)
          .addKeyValue("subscribed", subscribed)
          .addKeyValue("exception", exception)
          .log();
    }
  }
}

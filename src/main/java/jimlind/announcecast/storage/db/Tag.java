package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.PreparedStatement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Tag {
  private @Inject Connection connection;

  public void addTag(String feedId, String roleId, String channelId, String userId) {
    String sql =
        "INSERT OR IGNORE INTO tag(feed_id, role_id, channel_id, user_id) VALUES(?, ?, ?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, feedId);
      statement.setString(2, roleId);
      statement.setString(3, channelId);
      statement.setString(4, userId);
      statement.execute();
    } catch (Exception exception) {
      log.atWarn()
          .setMessage("Unable to add tag")
          .addKeyValue("feedId", feedId)
          .addKeyValue("roleId", roleId)
          .addKeyValue("channelId", channelId)
          .addKeyValue("userId", userId)
          .addKeyValue("exception", exception)
          .log();
    }
  }

  public String getTagByFeedId(String feedId, String channelId) {
    String sql =
        """
        SELECT tag.role_id FROM tag
        INNER JOIN patreon ON tag.user_id = patreon.user_id
        WHERE tag.feed_id = ? AND tag.channel_id = ?
        """;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, feedId);
      statement.setString(2, channelId);
      var result = statement.executeQuery();
      if (result.next()) {
        return result.getString("role_id");
      }
    } catch (Exception exception) {
      log.atWarn()
          .setMessage("Unable to get tag")
          .addKeyValue("feedId", feedId)
          .addKeyValue("channelId", channelId)
          .addKeyValue("exception", exception)
          .log();
    }
    return null;
  }
}

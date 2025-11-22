package jimlind.announcecast.storage.db;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class Tag {
  private final Connection connection;

  @Inject
  public Tag(Connection connection) {
    this.connection = connection;
  }

  public void addTag(String feedId, String roleId, String channelId, String userId) {
    String sql =
        "INSERT OR IGNORE INTO tag(feed_id, role_id, channel_id, user_id) VALUES(?, ?, ?, ?)";
    try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
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

  public void removeTags(String feedId, String channelId) {
    String sql = "DELETE FROM tag WHERE feed_id = ? AND channel_id = ?";
    try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
      statement.setString(1, feedId);
      statement.setString(2, channelId);
      statement.execute();
    } catch (Exception exception) {
      log.atWarn()
          .setMessage("Unable to remove tag")
          .addKeyValue("feedId", feedId)
          .addKeyValue("channelId", channelId)
          .addKeyValue("exception", exception)
          .log();
    }
  }

  public List<jimlind.announcecast.storage.model.Tag> getTagsByUserId(String userId) {
    List<jimlind.announcecast.storage.model.Tag> results = new ArrayList<>();

    String sql = "SELECT feed_id, role_id, channel_id FROM tag WHERE user_id = ?";
    try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
      statement.setString(1, userId);
      var resultSet = statement.executeQuery();
      while (resultSet.next()) {
        jimlind.announcecast.storage.model.Tag tag = new jimlind.announcecast.storage.model.Tag();
        tag.setFeedId(resultSet.getString("feed_id"));
        tag.setRoleId(resultSet.getString("role_id"));
        tag.setChannelId(resultSet.getString("channel_id"));

        results.add(tag);
      }
    } catch (Exception exception) {
      log.atWarn()
          .setMessage("Unable to get tags")
          .addKeyValue("userId", userId)
          .addKeyValue("exception", exception)
          .log();
    }

    return results;
  }
}

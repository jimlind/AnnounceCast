package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.PreparedStatement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Patreon {
  private @Inject Connection connection;

  public void insertMember(String patreonId, String userId) {
    String insertMember = "INSERT OR IGNORE INTO patreon (patreon_id, user_id) VALUES (?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(insertMember)) {
      statement.setString(1, patreonId);
      statement.setString(1, userId);
      statement.executeUpdate();
    } catch (Exception exception) {
      log.atWarn()
          .setMessage("Unable to insert patreon member")
          .addKeyValue("patreonId", patreonId)
          .addKeyValue("userId", userId)
          .addKeyValue("exception", exception)
          .log();
    }
  }

  public void deleteMemberByPatreonId(String patreonId) {
    String sql = "DELETE FROM patreon WHERE patreon_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, patreonId);
      statement.executeUpdate();
    } catch (Exception exception) {
      log.atWarn()
          .setMessage("Unable to delete patreon member")
          .addKeyValue("patreonId", patreonId)
          .addKeyValue("exception", exception)
          .log();
    }
  }
}

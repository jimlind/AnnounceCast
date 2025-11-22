package jimlind.announcecast.storage.db;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import jimlind.announcecast.patreon.PatreonMember;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class Patreon {
  private final Connection connection;

  @Inject
  public Patreon(Connection connection) {
    this.connection = connection;
  }

  public boolean userIdExists(String userId) {
    String sql = "SELECT COUNT(user_id) as value FROM patreon WHERE user_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, userId);
      ResultSet resultSet = statement.executeQuery();
      resultSet.next();
      int count = resultSet.getInt("value");
      return count > 0;
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to check user id existence").log();
    }
    return false;
  }

  public List<PatreonMember> getAllMembers() {
    List<PatreonMember> result = new ArrayList<>();
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT patreon_id, user_id FROM patreon");
      while (resultSet.next()) {
        PatreonMember member = new PatreonMember();
        member.setPatreonId(resultSet.getString("patreon_id"));
        member.setUserId(resultSet.getString("user_id"));
        member.setFullName("N/A");
        result.add(member);
      }
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to get all members").log();
    }

    return result;
  }

  public void insertMember(String patreonId, String userId) {
    String sql = "INSERT OR IGNORE INTO patreon (patreon_id, user_id) VALUES (?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, patreonId);
      statement.setString(2, userId);
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

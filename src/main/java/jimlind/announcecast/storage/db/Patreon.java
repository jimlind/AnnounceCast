package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import jimlind.announcecast.patreon.Member;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Patreon {
  private @Inject Connection connection;

  public List<Member> getAllMembers() {
    List<Member> result = new ArrayList<>();
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT patreon_id, user_id FROM patreon");
      while (resultSet.next()) {
        Member member = new Member();
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
    String insertMember = "INSERT OR IGNORE INTO patreon (patreon_id, user_id) VALUES (?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(insertMember)) {
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

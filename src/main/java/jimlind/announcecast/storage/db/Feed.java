package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.*;

public class Feed {
  private @Inject jimlind.announcecast.storage.db.Connection connection;

  public long getCount() {
    int countValue = 0;
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM feeds");
      resultSet.next();
      countValue = resultSet.getInt(1);
    } catch (Exception ignore) {
      // TODO: Log the exception
    }
    return countValue;
  }

  public String addFeed(String feedUrl, String feedTitle) {
    String feedId = "";

    String insertSql =
        "INSERT OR IGNORE INTO feeds (id, url, title) VALUES (lower(hex(randomblob(3))), ?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
      statement.setString(1, feedUrl);
      statement.setString(2, feedTitle);
      statement.executeUpdate();
    } catch (Exception ignore) {
      // TODO: Log the exception
    }

    String selectSql = "SELECT id FROM feeds WHERE url = ? LIMIT 1";
    try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
      statement.setString(1, feedUrl);
      ResultSet resultSet = statement.executeQuery();
      resultSet.next();
      feedId = resultSet.getString("id");
    } catch (Exception ignore) {
      // TODO: Log the exception
    }

    return feedId;
  }
}

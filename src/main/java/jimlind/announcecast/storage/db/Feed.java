package jimlind.announcecast.storage.db;

import com.google.inject.Inject;
import java.sql.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Feed {
  private @Inject jimlind.announcecast.storage.db.Connection connection;

  public String getUrl(String feedId) {
    String url = "";
    String selectSql = "SELECT url FROM feeds WHERE id = ? LIMIT 1";
    try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
      statement.setString(1, feedId);
      ResultSet resultSet = statement.executeQuery();
      resultSet.next();
      url = resultSet.getString("url");
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to get feed url").addKeyValue("feedId", feedId).log();
    }
    return url;
  }

  public long getCount() {
    int countValue = 0;
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM feeds");
      resultSet.next();
      countValue = resultSet.getInt(1);
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to get the feed count").log();
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
      log.atWarn()
          .setMessage("Unable to add a new feed")
          .addKeyValue("feedUrl", feedUrl)
          .addKeyValue("feedTitle", feedTitle)
          .log();
    }

    String selectSql = "SELECT id FROM feeds WHERE url = ? LIMIT 1";
    try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
      statement.setString(1, feedUrl);
      ResultSet resultSet = statement.executeQuery();
      resultSet.next();
      feedId = resultSet.getString("id");
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to get feed id by url").addKeyValue("feedUrl", feedUrl).log();
    }

    return feedId;
  }

  public void deleteFeed(String feedId) {
    String deleteSql = "DELETE FROM feeds WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
      statement.setString(1, feedId);
      statement.executeUpdate();
    } catch (Exception ignore) {
      log.atWarn().setMessage("Unable to delete feed").addKeyValue("feedId", feedId).log();
    }
  }
}

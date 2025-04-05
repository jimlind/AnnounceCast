package jimlind.announcecast.storage.db;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Connection {
  static String DB_FILE_LOCATION = "jdbc:sqlite:db/podcasts.db";

  private java.sql.Connection connection;

  public Connection() {
    try {
      this.connection = DriverManager.getConnection(DB_FILE_LOCATION);
      Statement statement = this.connection.createStatement();
      statement.execute(
          "CREATE TABLE IF NOT EXISTS channels (feed_id TEXT, channel_id TEXT, UNIQUE(feed_id, channel_id))");
      statement.execute(
          "CREATE TABLE IF NOT EXISTS feeds (id TEXT PRIMARY KEY, url TEXT UNIQUE, title TEXT)");
      statement.execute(
          "CREATE TABLE IF NOT EXISTS patreon (patreon_id TEXT PRIMARY KEY UNIQUE, user_id TEXT)");
      statement.execute(
          "CREATE TABLE IF NOT EXISTS posted (feed_id TEXT PRIMARY KEY UNIQUE, guid TEXT)");
      statement.execute("CREATE TABLE IF NOT EXISTS promoted_feed (feed_id TEXT, user_id TEXT)");
    } catch (SQLException ignore) {
      // If there isn't a database connection hard stop
      log.atError().setMessage("Setting Up SQLite failed").log();
      System.exit(-1);
    }

    List<String> indexSqlList = new ArrayList<>();
    indexSqlList.add("CREATE INDEX idx_channels_feed_id ON channels(feed_id)");
    indexSqlList.add("CREATE INDEX idx_feeds_id ON feeds(id)");
    indexSqlList.add("CREATE UNIQUE INDEX idx_patreon_user_id ON patreon(user_id)");
    indexSqlList.add("CREATE UNIQUE INDEX idx_posted_feed_id ON posted(feed_id)");
    indexSqlList.add("CREATE INDEX idx_promoted_feed_feed_id ON promoted_feed(feed_id)");

    for (String sql : indexSqlList) {
      try {
        this.connection.createStatement().execute(sql);
      } catch (SQLException ignore) {
        // Do nothing if there is an issue creating indexes
      }
    }
  }

  public Statement createStatement() throws Exception {
    try {
      return this.connection.createStatement();
    } catch (SQLException e) {
      String logMessage = "Unable to create statement";
      log.atWarn().setMessage(logMessage).log();
      throw new Exception(logMessage);
    }
  }

  public PreparedStatement prepareStatement(String sql) throws Exception {
    try {
      return this.connection.prepareStatement(sql);
    } catch (SQLException e) {
      String logMessage = "Unable to prepare statement";
      log.atWarn().setMessage(logMessage).addKeyValue("statement", sql).log();
      throw new Exception(logMessage);
    }
  }
}

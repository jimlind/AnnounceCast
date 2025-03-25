package jimlind.announcecast.storage.db;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
          "CREATE TABLE IF NOT EXISTS posted (feed_id TEXT PRIMARY KEY UNIQUE, guid TEXT)");
      statement.execute(
          "CREATE TABLE IF NOT EXISTS subscriber (feed_id TEXT PRIMARY KEY UNIQUE, active INTEGER NOT NULL CHECK (active IN (0, 1)), user_id TEXT)");
      statement.execute(
          "CREATE TABLE IF NOT EXISTS patreon (patreon_id TEXT PRIMARY KEY UNIQUE, user_id TEXT)");
    } catch (SQLException ignore) {
      // If there isn't a database connection hard stop
      log.atError().setMessage("Setting Up SQLite failed").log();
      System.exit(-1);
    }

    try {
      Statement indexStatement = this.connection.createStatement();
      indexStatement.execute("CREATE INDEX idx_channels_feed_id ON channels(feed_id)");
      indexStatement.execute("CREATE INDEX idx_feeds_id ON feeds(id)");
      indexStatement.execute("CREATE UNIQUE INDEX idx_posted_feed_id ON posted(feed_id)");
      indexStatement.execute("CREATE UNIQUE INDEX idx_subscriber_feed_id ON subscriber(feed_id)");
      indexStatement.execute("CREATE UNIQUE INDEX idx_patreon_user_id ON patreon(user_id)");
    } catch (SQLException ignore) {
      // Do nothing if there is an issue creating indexes
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

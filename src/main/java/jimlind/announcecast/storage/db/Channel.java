package jimlind.announcecast.storage.db;

import java.sql.*;
import java.sql.Connection;

public class Channel {
  public long getCount() {
    String url = "jdbc:sqlite:db/podcasts.db";
    java.sql.Connection conn;
    try {
      conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
      System.out.println("Connection to SQLite failed!");
      return 0;
    }

    String sql = "SELECT COUNT(*) FROM channel";
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      rs.next();
      int count = rs.getInt(1);
      stmt.close();
      return count;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void addChannel(String feedId, String channelId) {
    String url = "jdbc:sqlite:db/podcasts.db";
    Connection conn;
    try {
      conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
      System.out.println("Connection to SQLite failed!");
      return;
    }

    String sql = "INSERT OR IGNORE INTO channels (feed_id, channel_id) VALUES (?, ?)";
    try {
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, feedId);
      stmt.setString(2, channelId);
      stmt.executeUpdate();
      stmt.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}

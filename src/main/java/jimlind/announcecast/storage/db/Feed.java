package jimlind.announcecast.storage.db;

import java.sql.*;
import java.sql.Connection;

public class Feed {
  public long getCount() {
    String url = "jdbc:sqlite:db/podcasts.db";
    Connection conn;
    try {
      conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
      System.out.println("Connection to SQLite failed!");
      return 0;
    }

    String sql = "SELECT COUNT(*) FROM feeds";
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

  public String addFeed(String feedUrl, String feedTitle) {
    String url = "jdbc:sqlite:db/podcasts.db";
    Connection conn;
    try {
      conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
      System.out.println("Connection to SQLite failed!");
      return "";
    }

    String sql =
        "INSERT OR IGNORE INTO feeds (id, url, title) VALUES (lower(hex(randomblob(3))), ?, ?)";
    try {
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, feedUrl);
      stmt.setString(2, feedTitle);
      stmt.executeUpdate();
      stmt.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    String selectQuery = "SELECT id FROM feeds WHERE url = ? LIMIT 1";
    try {
      PreparedStatement selectStatement = conn.prepareStatement(selectQuery);
      selectStatement.setString(1, feedUrl);
      ResultSet rs = selectStatement.executeQuery();
      rs.next();
      String id = rs.getString("id");
      selectStatement.close();
      conn.close();
      return id;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}

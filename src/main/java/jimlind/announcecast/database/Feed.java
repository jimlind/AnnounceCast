package jimlind.announcecast.database;

import java.sql.*;

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
      return rs.getInt(1);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

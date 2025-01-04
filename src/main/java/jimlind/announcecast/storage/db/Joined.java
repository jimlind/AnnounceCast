package jimlind.announcecast.storage.db;

import java.sql.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jimlind.announcecast.storage.model.Feed;

public class Joined {
  public List<Feed> getFeedsByChannelId(String channelId) {
    String url = "jdbc:sqlite:db/podcasts.db";
    Connection conn;
    try {
      conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
      System.out.println("Connection to SQLite failed!");
      return Collections.emptyList();
    }

    String sql =
        "SELECT id, title FROM feeds INNER JOIN channels ON feeds.id = channels.feed_id WHERE channel_id = ? ORDER BY title";
    List<Feed> feedList = new ArrayList<Feed>();
    try {
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, channelId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        Feed feed = new Feed();
        feed.setId(rs.getString("id"));
        feed.setTitle(rs.getString("title"));
        feedList.add(feed);
      }
    } catch (Exception e) {
      System.out.println(e);
      return Collections.emptyList();
    }
    return feedList;
  }
}

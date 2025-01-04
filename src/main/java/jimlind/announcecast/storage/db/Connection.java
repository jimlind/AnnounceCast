package jimlind.announcecast.storage.db;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Connection {
  static String DB_FILE_LOCATION = "jdbc:sqlite:db/podcasts.db";

  private java.sql.Connection connection;

  public Connection() {
    try {
      this.connection = DriverManager.getConnection(DB_FILE_LOCATION);
    } catch (SQLException e) {
      // If there isn't a database connection hard stop
      System.out.println("Connection to SQLite failed!");
      System.exit(-1);
    }
  }

  public Statement createStatement() throws Exception {
    try {
      return this.connection.createStatement();
    } catch (SQLException e) {
      throw new Exception("Unable to create statement.");
    }
  }

  public PreparedStatement prepareStatement(String sql) throws Exception {
    try {
      return this.connection.prepareStatement(sql);
    } catch (SQLException e) {
      throw new Exception("Unable to prepare statement. " + sql);
    }
  }
}

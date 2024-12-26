package jimlind.announcecast;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello Main World!");

        Injector injector = Guice.createInjector(new BasicModule());
        Discord discord = injector.getInstance(Discord.class);
        discord.run();

        int pageSize = 10;
        String url = "jdbc:sqlite:db/podcasts.db";
        Connection conn;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Connection to SQLite failed!");
            return;
        }

        Timer timer = new Timer();
        TimerTask task =
                new TimerTask() {
                    private int pageNumber = 1;
                    public void run() {
                        int offset = (pageNumber - 1) * pageSize;
                        String sql = "SELECT id, url, title FROM feeds LIMIT ? OFFSET ?";
                        try {
                            System.out.println(offset);
                            PreparedStatement stmt = conn.prepareStatement(sql);
                            stmt.setInt(1, pageSize);
                            stmt.setInt(2, offset);
                            ResultSet rs = stmt.executeQuery();

                            if (!rs.isBeforeFirst() ) {
                                this.pageNumber = 1;
                            } else {
                                this.pageNumber++;
                            }

                            while (rs.next()) {
                                new PodcastParser().run(rs.getString("url"));
                            }

                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }


                        System.out.println("------");
                    }
                };
        timer.scheduleAtFixedRate(task, 0, 2000);
    }
}

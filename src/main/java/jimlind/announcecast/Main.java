package jimlind.announcecast;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import jimlind.announcecast.podcast.Client;
import jimlind.announcecast.podcast.Episode;
import jimlind.announcecast.podcast.Podcast;

public class Main {
  static int PAGE_SIZE = 10;
  static int PAGINATION_DELAY = 2000;

  public static void main(String[] args) {
    Properties properties = new Properties();
    try (FileInputStream input = new FileInputStream(args[0])) {
      properties.load(input);
    } catch (Exception e) {
      System.out.println("Application expects a formatted properties files as only argument");
      System.exit(-1);
    }

    Injector injector =
        Guice.createInjector(
            new BasicModule(),
            new jimlind.announcecast.storage.BasicModule(),
            new jimlind.announcecast.discord.BasicModule(),
            new jimlind.announcecast.podcast.BasicModule(),
            new jimlind.announcecast.integration.BasicModule());

    Discord discord = injector.getInstance(Discord.class);
    discord.run(properties.getProperty("DISCORD_BOT_TOKEN"));

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

          @Override
          public void run() {
            String sql = "SELECT id, url, title FROM feeds LIMIT ? OFFSET ?";
            try {
              System.out.println(pageNumber);
              PreparedStatement stmt = conn.prepareStatement(sql);
              stmt.setInt(1, PAGE_SIZE);
              stmt.setInt(2, (pageNumber - 1) * PAGE_SIZE);
              ResultSet rs = stmt.executeQuery();

              if (!rs.isBeforeFirst()) {
                this.pageNumber = 1;
              } else {
                this.pageNumber++;
              }

              while (rs.next()) {
                Client podcastClient = injector.getInstance(Client.class);
                Podcast podcast = podcastClient.createPodcastFromFeedUrl(rs.getString("url"), 5);
                if (podcast == null) {
                  // There was some issue loading the podcast from the feed but this happens for a
                  // multitude of reasons related for connectivity of formatting, so I'll just
                  // ignore them all for now and assume users will report any interesting issues
                  continue;
                }

                System.out.println(podcast.getTitle());
                System.out.println(podcast.getAuthor());
                System.out.println(podcast.getShowUrl());
                System.out.println(podcast.getImageUrl());

                for (Episode episode : podcast.getEpisodeList()) {
                  System.out.println(episode.getTitle());
                  System.out.println(episode.getLink());
                }
              }

            } catch (Exception e) {
              System.out.println(e.getMessage());
            }

            System.out.println("------");
          }
        };
    // Don't start the podcast scraping looper
    // timer.scheduleAtFixedRate(task, 0, PAGINATION_DELAY);
  }
}

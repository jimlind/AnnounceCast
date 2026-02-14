package jimlind.announcecast.podcast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.jetbrains.annotations.Nullable;

@Singleton
public class Client {
  private final Parser parser;
  int CONNECTION_CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(2);
  int CONNECTION_READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(3);
  int XMLSTREAM_READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(2);

  @Inject
  public Client(Parser parser) {
    this.parser = parser;
  }

  public @Nullable Podcast createPodcastFromFeedUrl(String feed, int episodeCount) {
    return this.createPodcastFromFeedUrl(feed, episodeCount, 1);
  }

  public @Nullable Podcast createPodcastFromFeedUrl(String feed, int episodeCount, int timeoutX) {
    String title = getClass().getPackage().getImplementationTitle();
    String version = getClass().getPackage().getImplementationVersion();

    HttpURLConnection connection;
    try {
      URL url = new URI(feed).toURL();
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestProperty("Host", url.getHost());
      connection.setRequestProperty("User-Agent", title + "/" + version);
      connection.setConnectTimeout(CONNECTION_CONNECT_TIMEOUT * timeoutX);
      connection.setReadTimeout(CONNECTION_READ_TIMEOUT * timeoutX);
    } catch (Exception ignored) {
      // Ignore any errors from attempting to build a URL or open a connection
      // It isn't my role to validate the whole Internet
      return null;
    }

    try {
      int responseCode = connection.getResponseCode();
      if (responseCode > 299 && responseCode < 400) {
        return createPodcastFromFeedUrl(
            connection.getHeaderField("Location"), episodeCount, timeoutX);
      } else if (responseCode >= 400) {
        return null;
      }
    } catch (Exception ignored) {
      // Ignore any errors from trying to get a response code
      // It isn't my roles to validate the whole Internet
      return null;
    }

    InputStream inputStream;
    try {
      inputStream = connection.getInputStream();
    } catch (Exception ignored) {
      // Ignore any errors from timeouts trying to connect to the input steam
      // It isn't my role to validate the whole Internet
      return null;
    }

    XMLStreamReader xmlStreamReader;
    try {
      XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
      xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
    } catch (Exception ignored) {
      // Ignore any errors from creating the xml stream
      // It isn't my role to validate feed formatting
      return null;
    }

    // Create a timer to close the stream if it doesn't complete quickly
    // Don't worry about anything else that it does.
    Timer timer = new Timer(true);
    TimerTask task =
        new TimerTask() {
          @Override
          public void run() {
            try {
              inputStream.close();
              xmlStreamReader.close();
            } catch (Exception ignored) {
            }
          }
        };
    timer.schedule(task, (long) XMLSTREAM_READ_TIMEOUT * timeoutX);

    // This will never error out, and will always get some kind of Podcast/null object
    Podcast podcast = this.parser.processStreamReader(xmlStreamReader, episodeCount);
    podcast.setFeedUrl(feed);

    try {
      timer.cancel(); // Cancel the timer if processing finished
      inputStream.close(); // Close the inputStream
      xmlStreamReader.close(); // Close the xmlStreamReader if processing finished
    } catch (Exception ignored) {
      // Ignore any problems closing or cancelling
      // They only happen if something is already closed or cancelled
    }

    if (!podcast.isValid()) {
      return null;
    }

    return podcast;
  }
}

package jimlind.announcecast.podcast;

import com.google.inject.Inject;
import java.io.InputStream;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.jetbrains.annotations.Nullable;

public class Client {
  int CONNECTION_CONNECT_TIMEOUT = 500; // 0.5s
  int CONNECTION_READ_TIMEOUT = 500; // 0.5s
  int XMLSTREAM_READ_TIMEOUT = 500; // 0.5s

  @Inject private Parser parser;

  public @Nullable Podcast createPodcastFromFeedUrl(String feed, int episodeCount) {
    String title = getClass().getPackage().getImplementationTitle();
    String version = getClass().getPackage().getImplementationVersion();

    HttpURLConnection connection;
    try {
      URL url = new URI(feed).toURL();
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestProperty("Host", url.getHost());
      connection.setRequestProperty("User-Agent", title + "/" + version);
      connection.setConnectTimeout(CONNECTION_CONNECT_TIMEOUT);
      connection.setReadTimeout(CONNECTION_READ_TIMEOUT);
    } catch (Exception ignored) {
      // Ignore any errors from attempting to build a URL or open a connection
      // It isn't my role to validate the whole Internet
      return null;
    }

    try {
      int responseCode = connection.getResponseCode();
      if (responseCode > 299 && responseCode < 400) {
        return createPodcastFromFeedUrl(connection.getHeaderField("Location"), episodeCount);
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
    timer.schedule(task, XMLSTREAM_READ_TIMEOUT);

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

    return podcast;
  }
}

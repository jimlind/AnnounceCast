package jimlind.announcecast.podcast;

import com.google.inject.Inject;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class Client {
  int CONNECTION_CONNECT_TIMEOUT = 500; // 0.5s
  int CONNECTION_READ_TIMEOUT = 500; // 0.5s
  int XMLSTREAM_READ_TIMEOUT = 500; // 0.5s

  @Inject private Parser parser;

  public Podcast createPodcastFromFeedUrl(String feed, int episodeCount) {
    URLConnection connection;
    try {
      connection = new URI(feed).toURL().openConnection();
    } catch (Exception ignored) {
      // Ignore any errors from attempting to build a URL or open a connection
      // It isn't my role to validate the whole Internet
      return null;
    }

    InputStream inputStream;
    connection.setConnectTimeout(CONNECTION_CONNECT_TIMEOUT);
    connection.setReadTimeout(CONNECTION_READ_TIMEOUT);
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

    Podcast podcast;
    try {
      podcast = this.parser.processStreamReader(xmlStreamReader, episodeCount);
    } catch (Exception e) {
      // TODO: Log the failure here. I want to know about it.
      System.out.println(e);
      return null;
    }

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

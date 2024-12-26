package jimlind.announcecast;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

public class PodcastParser {
    public void run(String feed) throws Exception {
        URI feedUri = new URI(feed);
        URL feedUrl = feedUri.toURL();

        URLConnection connection = feedUrl.openConnection();
        connection.setConnectTimeout(5000); // 5000 ms
        connection.setReadTimeout(5000); // 5000 ms
        InputStream inputStream = connection.getInputStream();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Timeout occurred while reading XML.");
                try {
                    reader.close(); // Close the reader on timeout
                } catch (XMLStreamException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        timer.schedule(task, 500); // Set timeout to 0.5 seconds

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT && "item".equals(reader.getLocalName())) {
                String title = null;
                String link = null;
                String description = null;
                String enclosureUrl = null;

                while (reader.hasNext()) {
                    event = reader.next();

                    if (event == XMLStreamConstants.START_ELEMENT) {
                        String elementName = reader.getLocalName();

                        if ("title".equals(elementName)) {
                            title = reader.getElementText();
                        } else if ("link".equals(elementName)) {
                            link = reader.getElementText();
                        } else if ("description".equals(elementName)) {
                            description = reader.getElementText();
                        } else if ("enclosure".equals(elementName)) {
                            enclosureUrl = reader.getAttributeValue(null, "url");
                        }
                    }
                    if (event == XMLStreamConstants.END_ELEMENT && "item".equals(reader.getLocalName())) {
                        break;  // exit the inner loop for the current <item>
                    }
                }

                // Print parsed podcast information
                System.out.println("Title: " + title);
                System.out.println("Link: " + link);
                System.out.println("Description: " + description);
                System.out.println("Enclosure URL: " + enclosureUrl);
                break;
            }
        }

        timer.cancel(); // Cancel the timer if processing finishes before timeout

        reader.close();
        inputStream.close();
    }
}


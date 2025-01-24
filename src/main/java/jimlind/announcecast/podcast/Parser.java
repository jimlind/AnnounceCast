package jimlind.announcecast.podcast;

import java.util.Stack;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class Parser {
  private static boolean isChildOfChannel(Stack<String> elementStack) {
    if (elementStack.isEmpty()) {
      return false;
    }

    return elementStack.peek().equals("channel");
  }

  private static boolean isChildOfItem(Stack<String> elementStack) {
    if (elementStack.isEmpty()) {
      return false;
    }

    return elementStack.peek().equals("item");
  }

  public Podcast processStreamReader(XMLStreamReader xmlStreamReader, int episodeCount) {
    Podcast podcast = new Podcast();
    Episode episode = new Episode();
    Stack<String> elementStack = new Stack<>();

    try {
      while (xmlStreamReader.hasNext()) {
        int event = xmlStreamReader.next();
        if (event == XMLStreamConstants.START_ELEMENT) {
          String localElementName = xmlStreamReader.getLocalName();
          String elementPrefix = xmlStreamReader.getPrefix();
          String qualifiedElementName =
              elementPrefix.isBlank() ? localElementName : elementPrefix + ":" + localElementName;

          switch (qualifiedElementName) {
            case "link":
              String linkText = xmlStreamReader.getElementText();
              if (isChildOfChannel(elementStack)) {
                podcast.setShowUrl(linkText);
              } else if (isChildOfItem(elementStack)) {
                episode.setLink(linkText);
              }
              continue;
          }

          switch (localElementName) {
            case "title":
              String titleText = xmlStreamReader.getElementText();
              if (isChildOfChannel(elementStack)) {
                podcast.setTitle(titleText);
              } else if (isChildOfItem(elementStack)) {
                episode.setTitle(titleText);
              }
              break;
            case "description":
              String descriptionText = xmlStreamReader.getElementText();
              if (isChildOfChannel(elementStack)) {
                podcast.setDescription(descriptionText);
              } else if (isChildOfItem(elementStack)) {
                episode.setDescription(descriptionText);
              }
              break;
            case "guid":
              String guidText = xmlStreamReader.getElementText();
              if (isChildOfItem(elementStack)) {
                episode.setGuid(guidText);
              }
              break;
            case "summary":
              String summaryText = xmlStreamReader.getElementText();
              if (isChildOfChannel(elementStack)) {
                podcast.setSummary(summaryText);
              } else if (isChildOfItem(elementStack)) {
                episode.setSummary(summaryText);
              }
              break;
            case "author":
              String authorText = xmlStreamReader.getElementText();
              if (isChildOfChannel(elementStack)) {
                podcast.setAuthor(authorText);
              }
              break;
            case "image":
              for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                if (xmlStreamReader.getAttributeLocalName(i).equals("href")) {
                  if (isChildOfChannel(elementStack)) {
                    podcast.setImageUrl(xmlStreamReader.getAttributeValue(i));
                  } else if (isChildOfItem(elementStack)) {
                    episode.setImageUrl(xmlStreamReader.getAttributeValue(i));
                  }
                  break;
                }
              }
              elementStack.push(localElementName);
              break;
            case "season":
              String seasonText = xmlStreamReader.getElementText();
              episode.setSeasonId(seasonText);
              break;
            case "episode":
              String episodeText = xmlStreamReader.getElementText();
              episode.setEpisodeId(episodeText);
              break;
            case "duration":
              String durationText = xmlStreamReader.getElementText();
              episode.setDuration(durationText);
              break;
            case "explicit":
              String explicitText = xmlStreamReader.getElementText();
              episode.setExplicit(explicitText);
              break;
            case "item":
              // If we don't want any episodes return the podcast when first item is reached
              if (episodeCount == 0) {
                return podcast;
              }

              episode = new Episode();
              elementStack.push(localElementName);
              break;
            default:
              elementStack.push(localElementName);
              break;
          }
        }

        if (event == XMLStreamConstants.END_ELEMENT) {
          String localElementName = xmlStreamReader.getLocalName();
          if (localElementName.equals("item")) {
            podcast.addEpisode(episode);
            if (--episodeCount <= 0) {
              break;
            }
          }
          elementStack.pop();
        }
      }
    } catch (Exception ignored) {
      // When the XML Stream Reader is closed it throws errors. We should ignore those errors so
      // that any podcast information that was created can be returned
    }

    return podcast;
  }
}

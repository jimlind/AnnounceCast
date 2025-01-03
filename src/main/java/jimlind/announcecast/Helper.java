package jimlind.announcecast;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Helper {

  public static String htmlToMarkdown(String htmlInputString, int length) {
    String markdownSuffix = "";
    if (htmlInputString.length() > length) {
      htmlInputString = htmlInputString.substring(0, length).trim();
      markdownSuffix = "...";
    }

    Document document = Jsoup.parseBodyFragment(htmlInputString);
    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    String markdownText = new CopyDown(options).convert(document.body().toString());

    return markdownText + markdownSuffix;
  }
}

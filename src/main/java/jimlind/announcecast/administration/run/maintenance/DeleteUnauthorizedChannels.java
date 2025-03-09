package jimlind.announcecast.administration.run.maintenance;

import com.google.inject.Inject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import jimlind.announcecast.administration.Helper;
import jimlind.announcecast.storage.db.Channel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class DeleteUnauthorizedChannels {
  private final Channel channel;
  private final Helper helper;
  private final Scanner scanner;

  @Inject
  DeleteUnauthorizedChannels(Channel channel, Helper helper) {
    this.channel = channel;
    this.helper = helper;
    this.scanner = new Scanner(System.in);
  }

  public void run() throws Exception {
    System.out.print("Bot Token? (0PFtbdlKtu...): ");
    String botToken = this.scanner.nextLine();

    JDA jda = JDABuilder.createLight(botToken).build();
    jda.awaitReady();

    List<String> channelIdList = this.channel.getUniqueChannelIds();
    for (int i = 0; i < 10; i++) {
      Iterator<String> iterator = channelIdList.iterator();
      while (iterator.hasNext()) {
        boolean hasCorrectPermissions = this.helper.hasCorrectPermissions(jda, iterator.next());
        if (hasCorrectPermissions) {
          iterator.remove();
        }
      }
    }
    System.out.println("Found " + channelIdList.size() + " unauthorized channels");
    System.out.print("Delete and archive them? (yes, no): ");
    boolean delete = this.scanner.nextLine().equals("yes");
    if (!delete) {
      return;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    String outputFile =
        "log/deletes/channel_deletes_" + LocalDateTime.now().format(formatter) + ".txt";
    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

    for (String channelId : channelIdList) {
      System.out.println(channelId);

      for (String feedId : this.channel.getFeedsByChannelId(channelId)) {
        channel.deleteChannel(feedId, channelId);
        writer.write("feed:" + feedId + "|channel:" + channelId + "\n");
      }
    }
    writer.close();
  }
}

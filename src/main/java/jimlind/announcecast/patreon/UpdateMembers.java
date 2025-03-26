package jimlind.announcecast.patreon;

import com.google.inject.Inject;
import java.util.List;
import java.util.TimerTask;
import lombok.Setter;

public class UpdateMembers extends TimerTask {
  @Setter private String patreonAccessToken;
  @Inject private Client client;

  @Override
  public void run() {
    List<Member> memberList = this.client.createMemberList(this.patreonAccessToken);
    for (Member member : memberList) {
      System.out.println(member.getFullName());
      System.out.println(member.getPatreonId());
      System.out.println(member.getUserId());
    }
  }

  // scrape the patreon api
  // insert (or reinsert) members into local db
  // get all members from local db
  // loop over and delete when missing from the patreon api list
}

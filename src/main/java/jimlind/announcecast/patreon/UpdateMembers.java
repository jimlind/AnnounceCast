package jimlind.announcecast.patreon;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import jimlind.announcecast.storage.db.Patreon;
import lombok.Setter;

public class UpdateMembers extends TimerTask {
  @Setter private String patreonAccessToken;
  @Inject private Client client;
  @Inject private Patreon patreon;

  @Override
  public void run() {
    List<Member> memberList = this.client.createMemberList(this.patreonAccessToken);
    List<String> patreonIdList = new ArrayList<>();
    for (Member member : memberList) {
      this.patreon.insertMember(member.getPatreonId(), member.getUserId());
      patreonIdList.add(member.getPatreonId());
    }

    for (Member member : this.patreon.getAllMembers()) {
      if (!patreonIdList.contains(member.getPatreonId())) {
        this.patreon.deleteMemberByPatreonId(member.getPatreonId());
      }
    }
  }
}

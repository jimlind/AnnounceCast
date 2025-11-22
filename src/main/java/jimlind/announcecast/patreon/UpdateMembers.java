package jimlind.announcecast.patreon;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.announcecast.storage.db.Patreon;
import lombok.Setter;

@Singleton
public class UpdateMembers extends TimerTask {
  private final Client client;
  private final Patreon patreon;
  @Setter private String patreonAccessToken;

  @Inject
  public UpdateMembers(Client client, Patreon patreon) {
    this.client = client;
    this.patreon = patreon;
  }

  @Override
  public void run() {
    List<PatreonMember> memberList = this.client.createMemberList(this.patreonAccessToken);
    List<String> patreonIdList = new ArrayList<>();
    for (PatreonMember member : memberList) {
      this.patreon.insertMember(member.getPatreonId(), member.getUserId());
      patreonIdList.add(member.getPatreonId());
    }

    for (PatreonMember member : this.patreon.getAllMembers()) {
      if (!patreonIdList.contains(member.getPatreonId())) {
        this.patreon.deleteMemberByPatreonId(member.getPatreonId());
      }
    }
  }
}

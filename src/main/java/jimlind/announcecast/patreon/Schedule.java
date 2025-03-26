package jimlind.announcecast.patreon;

import com.google.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Schedule {
  public static final long PATREON_UPDATE_PERIOD = TimeUnit.HOURS.toMillis(2);

  @Inject private UpdateMembers updateMembers;

  public void startMemberUpdate(String patreonAccessToken) {
    updateMembers.setPatreonAccessToken(patreonAccessToken);
    new Timer().schedule(updateMembers, 0, PATREON_UPDATE_PERIOD);
  }
}

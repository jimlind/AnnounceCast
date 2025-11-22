package jimlind.announcecast.patreon;

import java.util.Timer;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class Schedule {
  public static final long PATREON_UPDATE_PERIOD = TimeUnit.HOURS.toMillis(2);

  private final UpdateMembers updateMembers;

  @Inject
  public Schedule(UpdateMembers updateMembers) {
    this.updateMembers = updateMembers;
  }

  public void startMemberUpdate(String patreonAccessToken) {
    updateMembers.setPatreonAccessToken(patreonAccessToken);
    new Timer().schedule(updateMembers, 0, PATREON_UPDATE_PERIOD);
  }
}

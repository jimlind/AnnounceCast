package jimlind.announcecast.core.taskScheduling;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
@Slf4j
public class Scheduler {
  private final AtomicBoolean scheduledTasksStarted = new AtomicBoolean(false);
  private final AtomicBoolean supervisorStarted = new AtomicBoolean(false);
  private final List<InfiniteTask> taskList = new ArrayList<>();
  private final ScheduledExecutorService supervisor = Executors.newSingleThreadScheduledExecutor();

  @Inject
  public Scheduler() {}

  public void addTask(InfiniteTask task) {
    taskList.add(task);
  }

  public void startAll() {
    if (!scheduledTasksStarted.compareAndSet(false, true)) {
      log.error("Failure: Scheduler can only be started once.");
      return;
    }

    taskList.forEach(InfiniteTask::start);

    if (supervisorStarted.compareAndSet(false, true)) {
      supervisor.scheduleAtFixedRate(this::restartAnyExitedTasks, 1, 1, TimeUnit.SECONDS);
    }
  }

  public void stopAll() {
    taskList.forEach(InfiniteTask::stop);
    scheduledTasksStarted.set(false);
  }

  private void restartAnyExitedTasks() {
    for (InfiniteTask task : taskList) {
      if (!task.hasExited()) {
        continue;
      }
      log.info("Restarting the exited task: {}", task.getClass().getSimpleName());
      task.start();
    }
  }
}

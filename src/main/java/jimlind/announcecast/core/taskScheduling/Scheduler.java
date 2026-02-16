package jimlind.announcecast.core.taskScheduling;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
  private final List<InfiniteTask> taskList = new ArrayList<>();

  public Scheduler() {}

  public void addTask(InfiniteTask task) {
    taskList.add(task);
  }

  public void startAll() {
    taskList.forEach(InfiniteTask::start);
  }

  public void stopAll() {
    taskList.forEach(InfiniteTask::stop);
  }
}

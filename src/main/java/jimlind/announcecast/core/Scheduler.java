package jimlind.announcecast.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Scheduler {

  private final ScheduledExecutorService scheduler;
  private final ExecutorService workerPool;
  private final ScheduledExecutorService supervisor;

  private final List<TaskDescriptor> tasks = new ArrayList<>();
  private final AtomicBoolean started = new AtomicBoolean(false);

  public Scheduler() {
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.workerPool = Executors.newCachedThreadPool();
    this.supervisor = Executors.newSingleThreadScheduledExecutor();
  }

  /**
   * Add a task to the task list.
   *
   * @param name Name of the task used for internal debugging. Uniqueness not needed.
   * @param task The runnable that will be executed on schedule.
   * @param delaySeconds Number of seconds to wait between each task run.
   * @param timeoutSeconds Number of seconds to allow the tak to run before cancelling it.
   */
  public void addTask(String name, Runnable task, long delaySeconds, long timeoutSeconds) {
    if (started.get()) {
      log.error("Failure: Tasks can not be added after scheduler starts.");
      return;
    }
    tasks.add(new TaskDescriptor(name, task, delaySeconds, timeoutSeconds));
  }

  /** Start the list of supplied tasks as well as the watchdog task. */
  public void start() {
    if (!started.compareAndSet(false, true)) {
      log.error("Failure: Scheduler can only be started once.");
      return;
    }

    tasks.forEach(this::scheduleTask);
    startSupervisor();
  }

  /**
   * Execute the task following a schedule.
   *
   * @param taskDescriptor Task with metadata
   */
  private void scheduleTask(TaskDescriptor taskDescriptor) {
    scheduler.scheduleWithFixedDelay(
        () -> runTaskWithTimeout(taskDescriptor), 0, taskDescriptor.delaySeconds, TimeUnit.SECONDS);
  }

  /**
   * Execute the task with a set timeout and catching any exceptions that might come from scheduling
   * a thread and potential thread failures.
   *
   * @param taskDescriptor Task with metadata
   */
  private void runTaskWithTimeout(TaskDescriptor taskDescriptor) {
    Future<?> future = workerPool.submit(() -> executeTask(taskDescriptor));
    try {
      future.get(taskDescriptor.timeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException timeoutException) {
      log.error("Task '{}' timed out", taskDescriptor.name);
      future.cancel(true);
    } catch (Exception exception) {
      log.error("Unexpected error in task '{}': {}", taskDescriptor.name, exception.getMessage());
    }
  }

  /**
   * Execute the task and catch logical exceptions from the task.
   *
   * @param taskDescriptor Task with metadata
   */
  private void executeTask(TaskDescriptor taskDescriptor) {
    try {
      taskDescriptor.task.run();
    } catch (Error error) {
      log.error("Fatal error in task '{}': {}", taskDescriptor.name, error.toString());
      throw error;
    } catch (Exception exception) {
      log.error("Task '{}' failed: {}", taskDescriptor.name, exception.getMessage());
    }
  }

  /** Creates a supervisor that runs every hour to check that the scheduler hasn't exited * */
  private void startSupervisor() {
    Runnable rebuildTasksIfSchedulerExited =
        () -> {
          if (scheduler.isShutdown() || scheduler.isTerminated()) {
            log.atInfo().setMessage("Rebuilding tasks because exited scheduler found.").log();
            tasks.forEach(this::scheduleTask);
          }
        };
    supervisor.scheduleAtFixedRate(rebuildTasksIfSchedulerExited, 1, 1, TimeUnit.HOURS);
  }

  // Object record with all necessary data as appropriate types to describe a task
  private record TaskDescriptor(
      String name, Runnable task, long delaySeconds, long timeoutSeconds) {}
}

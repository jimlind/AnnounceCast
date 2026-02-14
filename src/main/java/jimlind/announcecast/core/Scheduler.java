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
   * Add a task that runs periodically with a forced timeout of that period.
   *
   * @param name Name of the task used for internal debugging.
   * @param task The runnable that will be executed on schedule.
   * @param periodMillis The period between successive executions.
   */
  public void addPeriodicTask(String name, Runnable task, long periodMillis) {
    if (started.get()) {
      log.error("Failure: Periodic tasks can not be added after scheduler starts.");
      return;
    }
    tasks.add(new TaskDescriptor(name, task, periodMillis, 0, periodMillis));
  }

  /**
   * Add a task that runs once after a specific delay with a specific timeout.
   *
   * @param name Name of the task used for internal debugging.
   * @param task The runnable that will be executed.
   * @param delayMillis The delay before the task is executed.
   * @param timeoutMillis The maximum time allowed for the execution.
   */
  public void addDelayedTask(String name, Runnable task, long delayMillis, long timeoutMillis) {
    if (started.get()) {
      log.error("Failure: Delayed tasks can not be added after scheduler starts.");
      return;
    }
    tasks.add(new TaskDescriptor(name, task, 0, delayMillis, timeoutMillis));
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
    Runnable runnable = () -> runTaskWithTimeout(taskDescriptor);
    if (taskDescriptor.delayMillis == 0 && taskDescriptor.periodMillis > 0) {
      scheduler.scheduleAtFixedRate(
          runnable, 0, taskDescriptor.periodMillis, TimeUnit.MILLISECONDS);
    } else if (taskDescriptor.periodMillis == 0 && taskDescriptor.delayMillis > 0) {
      scheduler.scheduleWithFixedDelay(
          runnable, 0, taskDescriptor.delayMillis, TimeUnit.MILLISECONDS);
    } else {
      log.error("Failure: Invalid TaskDescriptor created.");
    }
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
      future.get(taskDescriptor.timeoutMillis, TimeUnit.MILLISECONDS);
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
  // This should probably be extracted to another class that validates periodic and delay
  private record TaskDescriptor(
      String name, Runnable task, long periodMillis, long delayMillis, long timeoutMillis) {}
}

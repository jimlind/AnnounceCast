package jimlind.announcecast.core.taskScheduling;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper for a ScheduledExecutorService task.
 *
 * <p>This serves two purposes: 1) To ensure that the task could theoretically be closed when they
 * are not no longer needed, making static analysis tools and 2) ensure that appropriate error and
 * throwable events are handled correctly.
 */
@Slf4j
public abstract class InfiniteTask {
  protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  protected ScheduledFuture<?> scheduledFuture;

  /**
   * Starts the ScheduledExecutorService. Needs to be implemented.
   *
   * <p>Should schedule running the `runSafely` method accordingly.
   */
  public abstract void start();

  /**
   * Stops the ScheduledExecutorService. Even though this method should not be executed in the
   * infinite use cases it satisfies static analysis tools needs by existing and good to keep around
   * if I ever want to make a task that isn't infinite.
   */
  public void stop() {
    if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
      scheduledFuture.cancel(false);
    }
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /** Execution would stop if this method returned true, but it is a hardcoded infinite task */
  protected boolean shouldStop() {
    return false;
  }

  /** Task to actually run. Needs to be implemented. */
  protected abstract void runTask();

  protected void runSafely() {
    // Halt running if the scheduled task stop requested
    if (shouldStop()) {
      stop();
      return;
    }

    // Wraps the `runTask` method catching exceptions so they don't stop the scheduled task running.
    // Do throw any errors after logging so that we have visibility into those events.
    try {
      runTask();
    } catch (Error error) {
      log.error("Fatal error thrown in scheduled task and task killed: {}", error.toString());
      throw error;
    } catch (Exception exception) {
      log.error("Exception thrown and caught in scheduled task: {}", exception.getMessage());
    }
  }
}

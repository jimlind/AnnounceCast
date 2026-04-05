package jimlind.announcecast.scraper.task;

import jimlind.amaranth.exception.TaskException;
import jimlind.amaranth.exception.TaskGeneralException;
import jimlind.amaranth.exception.TaskInterruptionException;
import jimlind.amaranth.exception.TaskSeriousException;
import jimlind.amaranth.exception.TaskTimeoutException;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class TaskExceptionHandler {
  private static final String MESSAGE = "message";
  private static final String TRACE = "trace";

  @Inject
  public TaskExceptionHandler() {}

  public void handle(TaskException exception) {
    switch (exception) {
      case TaskGeneralException taskException:
        if (log.isWarnEnabled()) {
          log.atWarn()
              .setMessage("General Exception Thrown")
              .addKeyValue(MESSAGE, exception.getMessage())
              .addKeyValue(TRACE, exception.getCause().getStackTrace())
              .log();
        }
        break;
      case TaskInterruptionException taskInterruptionException:
        if (log.isWarnEnabled()) {
          log.atWarn()
              .setMessage("Interruption Exception Thrown")
              .addKeyValue(MESSAGE, exception.getMessage())
              .addKeyValue(TRACE, exception.getCause().getStackTrace())
              .log();
        }
        break;
      case TaskSeriousException taskSeriousException:
        if (log.isErrorEnabled()) {
          log.atError()
              .setMessage("Serious Exception Thrown")
              .addKeyValue(MESSAGE, exception.getMessage())
              .addKeyValue(TRACE, exception.getCause().getStackTrace())
              .log();
        }
        break;
      case TaskTimeoutException taskTimeoutException:
        if (log.isInfoEnabled()) {
          log.atInfo()
              .setMessage("Timeout Exception Thrown")
              .addKeyValue(MESSAGE, exception.getMessage())
              .log();
        }
        break;
      default:
        if (log.isErrorEnabled()) {
          log.atError()
              .setMessage("Unidentified Exception Thrown")
              .addKeyValue(MESSAGE, exception.getMessage())
              .addKeyValue(TRACE, exception.getCause().getStackTrace())
              .log();
        }
    }
  }
}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client;

import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class OperationRetrier {

  private static final Integer DEFAULT_ATTEMPTS = 10;
  private static final Long DEFAULT_SLEEP_TIME = 30000L;

  private Long sleepTime;
  private Integer attempts;

  public Long getSleepTime() {
    return sleepTime == null ? DEFAULT_SLEEP_TIME : sleepTime;
  }

  public void setSleepTime(Long sleepTime) {
    checkArgument(sleepTime != null, "Sleep cannot be null");
    checkArgument(sleepTime > 0, "Sleep time should be positive");
    this.sleepTime = sleepTime;
  }

  public Integer getAttempts() {
    return attempts == null ? DEFAULT_ATTEMPTS : attempts;
  }

  public void setAttempts(Integer attempts) {
    checkArgument(attempts != null, "Attempts cannot be null");
    checkArgument(attempts > 0, "Attempts should be positive");
    this.attempts = attempts;
  }

  public void setTimeout(Long timeout) {
    setSleepTime(timeout == null ? getSleepTime() : (timeout / getAttempts()));
  }

  public interface RetriableOperation {

    /**
     * Runs the operation
     * 
     * @return true, if it should keep running
     */
    Boolean run();

    /**
     * The message to be used in case of retry exhausted
     * 
     * @return a message
     */
    default String getRetryExhaustedMessage() {
      return StringUtils.EMPTY;
    }
  }

  public void retry(RetriableOperation operation) throws InterruptedException, TimeoutException {
    int i = 0;
    boolean keepRunning = true;
    while (i < getAttempts() && keepRunning) {
      keepRunning = operation.run();

      if (keepRunning) {
        Thread.sleep(getSleepTime());
      }
      i++;
    }

    if (i == getAttempts() && keepRunning) {
      throw new TimeoutException("Maximum number of attempts [" + getAttempts() + "] has been exceeded. "
          + operation.getRetryExhaustedMessage());
    }

  }

}

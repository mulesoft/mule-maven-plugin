/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client;

import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.mule.tools.client.OperationRetrier.RetriableOperation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
class OperationRetrierTest {

  @Test
  void retryFail() {
    assertThatThrownBy(() -> {
      OperationRetrier operationRetrier = new OperationRetrier();
      operationRetrier.setAttempts(1);
      operationRetrier.setSleepTime(1000L);

      operationRetrier.retry(() -> true);
    }).isExactlyInstanceOf(TimeoutException.class);
  }

  @Test
  void retrySucceed() throws TimeoutException, InterruptedException {
    OperationRetrier operationRetrier = new OperationRetrier();
    operationRetrier.setAttempts(1);
    operationRetrier.setSleepTime(1000L);

    operationRetrier.retry(() -> false);
  }

  @Test
  void retryTwoTimesAndFail() {
    Integer maxAttempts = 2;

    OperationRetrier operationRetrier = new OperationRetrier();
    operationRetrier.setAttempts(maxAttempts);
    operationRetrier.setSleepTime(1L);


    CounterRetriableOperation retriableOperation = new CounterRetriableOperation(maxAttempts);

    try {
      operationRetrier.retry(retriableOperation);
    } catch (Exception e) {
      assertThat(retriableOperation.getCount()).isEqualTo(maxAttempts);
    }
  }

  @Test
  void retryTwoTimesAndSucced() throws TimeoutException, InterruptedException {
    Integer maxAttempts = 3;

    OperationRetrier operationRetrier = new OperationRetrier();
    operationRetrier.setAttempts(maxAttempts);
    operationRetrier.setSleepTime(1L);

    CounterRetriableOperation retriableOperation = new CounterRetriableOperation(maxAttempts);
    retriableOperation.setSuccedAt(2);

    operationRetrier.retry(retriableOperation);
    assertThat(retriableOperation.getCount()).isEqualTo(2);
  }

  static class CounterRetriableOperation implements RetriableOperation {

    private Integer count = 0;
    private final Integer maxAttempts;
    private Integer succedAt = -1;

    public CounterRetriableOperation(Integer maxAttempts) {
      this.maxAttempts = maxAttempts;
    }

    public void setSuccedAt(Integer succedAt) {
      this.succedAt = succedAt;
    }

    public Integer getCount() {
      return count;
    }

    @Override
    public Boolean run() {
      count++;
      if (succedAt == -1) {
        return count <= maxAttempts;
      } else {
        return !count.equals(succedAt);
      }
    }
  }
}

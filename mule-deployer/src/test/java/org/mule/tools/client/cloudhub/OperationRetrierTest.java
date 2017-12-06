/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.cloudhub;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.concurrent.TimeoutException;

import org.junit.Test;

import org.mule.tools.client.cloudhub.OpeartionRetrier.RetriableOperation;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class OperationRetrierTest {

  @Test(expected = TimeoutException.class)
  public void retryFail() throws TimeoutException, InterruptedException {
    OpeartionRetrier opeartionRetrier = new OpeartionRetrier();
    opeartionRetrier.setAttempts(1);
    opeartionRetrier.setSleepTime(Long.valueOf(1000));

    opeartionRetrier.retry(() -> true);
  }

  @Test
  public void retrySucceed() throws TimeoutException, InterruptedException {
    OpeartionRetrier opeartionRetrier = new OpeartionRetrier();
    opeartionRetrier.setAttempts(1);
    opeartionRetrier.setSleepTime(Long.valueOf(1000));

    opeartionRetrier.retry(() -> false);
  }

  @Test
  public void retryTwoTimesAndFail() {
    Integer maxAttempts = 2;

    OpeartionRetrier opeartionRetrier = new OpeartionRetrier();
    opeartionRetrier.setAttempts(maxAttempts);
    opeartionRetrier.setSleepTime(Long.valueOf(1));


    CounterRetriableOperation retriableOperation = new CounterRetriableOperation(maxAttempts);

    try {
      opeartionRetrier.retry(retriableOperation);
    } catch (Exception e) {
      assertThat(retriableOperation.getCount(), is(maxAttempts));
    }
  }

  @Test
  public void retryTwoTimesAndSucced() throws TimeoutException, InterruptedException {
    Integer maxAttempts = 3;

    OpeartionRetrier opeartionRetrier = new OpeartionRetrier();
    opeartionRetrier.setAttempts(maxAttempts);
    opeartionRetrier.setSleepTime(Long.valueOf(1));


    CounterRetriableOperation retriableOperation = new CounterRetriableOperation(maxAttempts);
    retriableOperation.setSuccedAt(2);


    opeartionRetrier.retry(retriableOperation);
    assertThat(retriableOperation.getCount(), is(2));
  }

  class CounterRetriableOperation implements RetriableOperation {

    private Integer count = 0;
    private Integer maxAttempts;

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
        if (count == succedAt) {
          return false;
        }
        return true;
      }
    }
  }



}

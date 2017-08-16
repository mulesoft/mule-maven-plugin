/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.client.standalone.controller.probing;

public class PollingProber implements Prober {

  private static final long DEFAULT_TIMEOUT = 1000;
  private static final long DEFAULT_POLLING_INTERVAL = 100;

  private final long timeoutMillis;
  private final long pollDelayMillis;

  public PollingProber(Long timeoutMillis, Long pollDelayMillis) {
    this.timeoutMillis = timeoutMillis == null ? DEFAULT_TIMEOUT : timeoutMillis;
    this.pollDelayMillis = pollDelayMillis == null ? DEFAULT_POLLING_INTERVAL : pollDelayMillis;
  }

  @Override
  public void check(Probe probe) {
    if (!poll(probe)) {
      throw new AssertionError(probe.describeFailure());
    }
  }

  private boolean poll(Probe probe) {
    Timeout timeout = new Timeout(timeoutMillis);

    while (true) {
      if (probe.isSatisfied()) {
        return true;
      } else if (timeout.hasTimedOut()) {
        return false;
      } else {
        waitFor(pollDelayMillis);
      }
    }
  }

  private void waitFor(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      throw new IllegalStateException("unexpected interrupt", e);
    }
  }

}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.exception;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("rawtypes")
class MuleControllerExceptionTest {

  private static final String EXCEPTION_MESSAGE = "Standalone failure";
  private final MuleControllerException muleControllerException = new MuleControllerException(EXCEPTION_MESSAGE);

  @Test
  void muleControllerExceptionNoMessageTest() throws MuleControllerException {
    testException(new MuleControllerException(), null, null, null);
  }

  @Test
  void muleControllerExceptionCustomMessageTest() throws MuleControllerException {
    testException(muleControllerException, null, null, EXCEPTION_MESSAGE);
  }

  @Test
  void muleControllerExceptionCustomMessageNullCauseTest() throws MuleControllerException {
    testException(new MuleControllerException((Throwable) null), null, null, null);
  }

  @Test
  void muleControllerExceptionMessageWithCauseTest() throws MuleControllerException {
    Exception exception =
        new MuleControllerException(new IllegalArgumentException("Timeout should be a non negative integer number"));
    testException(exception, null, IllegalArgumentException.class,
                  "java.lang.IllegalArgumentException: Timeout should be a non negative integer number");
  }

  @Test
  void muleControllerExceptionCustomMessageWithCauseTest() throws MuleControllerException {
    Exception exception =
        new MuleControllerException(EXCEPTION_MESSAGE,
                                    new IllegalArgumentException("Timeout should be a non negative integer number"));
    testException(exception, null, IllegalArgumentException.class, EXCEPTION_MESSAGE);
  }

  @Test
  void muleControllerExceptionCustomMessageWithNullCauseTest() throws MuleControllerException {
    testException(new MuleControllerException(EXCEPTION_MESSAGE, null), null, null, EXCEPTION_MESSAGE);
  }

  private void testException(Throwable throwable, Class clazz, Class cause, String message) {
    AbstractThrowableAssert<?, ? extends Throwable> throwableAssert = assertThatThrownBy(() -> {
      throw throwable;
    })
        .isExactlyInstanceOf(Optional.ofNullable(clazz).orElse(MuleControllerException.class))
        .hasMessage(message);

    if (Objects.nonNull(cause)) {
      throwableAssert.cause().isExactlyInstanceOf(cause);
    }
  }
}

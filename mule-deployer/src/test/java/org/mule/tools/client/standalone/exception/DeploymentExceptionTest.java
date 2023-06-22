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
import org.mule.tools.client.core.exception.DeploymentException;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentExceptionTest {

  private static final String EXCEPTION_MESSAGE = "Could not deploy";
  private final DeploymentException deploymentException = new DeploymentException(EXCEPTION_MESSAGE);

  @Test
  void deploymentExceptionCustomMessageTest() {
    testException(deploymentException, DeploymentException.class, null);
  }

  @Test
  void deploymentExceptionCustomMessageNullCauseTest() {
    deploymentException.initCause(null);
    testException(deploymentException, DeploymentException.class, null);
  }

  @Test
  void deploymentExceptionCustomMessageWithCauseTest() {
    deploymentException.initCause(new IllegalArgumentException());
    testException(deploymentException, DeploymentException.class, IllegalArgumentException.class);
  }

  private void testException(Throwable throwable, Class<?> clazz, Class<?> cause) {
    AbstractThrowableAssert<?, ? extends Throwable> throwableAssert = assertThatThrownBy(() -> {
      throw throwable;
    })
        .isExactlyInstanceOf(clazz)
        .hasMessage(EXCEPTION_MESSAGE);

    if (Objects.nonNull(cause)) {
      throwableAssert.cause().isExactlyInstanceOf(cause);
    }
  }
}

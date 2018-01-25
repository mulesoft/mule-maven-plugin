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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.instanceOf;

import org.mule.tools.client.core.exception.DeploymentException;

public class DeploymentExceptionTest {

  private static final String EXCEPTION_MESSAGE = "Could not deploy";
  private final DeploymentException deploymentException = new DeploymentException(EXCEPTION_MESSAGE);

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Before
  public void setUp() {
    expected.expect(DeploymentException.class);
    expected.expectMessage(EXCEPTION_MESSAGE);
  }

  @Test
  public void deploymentExceptionCustomMessageTest() throws DeploymentException {
    throw deploymentException;
  }


  @Test
  public void deploymentExceptionCustomMessageNullCauseTest() throws DeploymentException {
    deploymentException.initCause(null);
    throw deploymentException;
  }

  @Test
  public void deploymentExceptionCustomMessageWithCauseTest() throws DeploymentException {
    deploymentException.initCause(new IllegalArgumentException());
    expected.expectCause(instanceOf(IllegalArgumentException.class));
    throw deploymentException;
  }

}

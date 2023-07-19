/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.standalone.exception;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.instanceOf;

public class MuleControllerExceptionTest {

  private static final String EXCEPTION_MESSAGE = "Standalone failure";
  private final MuleControllerException muleControllerException = new MuleControllerException(EXCEPTION_MESSAGE);

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Before
  public void setUp() {
    expected.expect(MuleControllerException.class);
  }

  @Test
  public void muleControllerExceptionNoMessageTest() throws MuleControllerException {
    throw new MuleControllerException();
  }

  @Test
  public void muleControllerExceptionCustomMessageTest() throws MuleControllerException {
    expected.expectMessage(EXCEPTION_MESSAGE);
    throw muleControllerException;
  }

  @Test
  public void muleControllerExceptionCustomMessageNullCauseTest() throws MuleControllerException {
    throw new MuleControllerException((Throwable) null);
  }

  @Test
  public void muleControllerExceptionMessageWithCauseTest() throws MuleControllerException {
    expected.expectMessage("java.lang.IllegalArgumentException: Timeout should be a non negative integer number");
    expected.expectCause(instanceOf(IllegalArgumentException.class));
    throw new MuleControllerException(new IllegalArgumentException("Timeout should be a non negative integer number"));
  }

  @Test
  public void muleControllerExceptionCustomMessageWithCauseTest() throws MuleControllerException {
    expected.expectMessage(EXCEPTION_MESSAGE);
    expected.expectCause(instanceOf(IllegalArgumentException.class));
    throw new MuleControllerException(EXCEPTION_MESSAGE,
                                      new IllegalArgumentException("Timeout should be a non negative integer number"));
  }

  @Test
  public void muleControllerExceptionCustomMessageWithNullCauseTest() throws MuleControllerException {
    expected.expectMessage(EXCEPTION_MESSAGE);
    throw new MuleControllerException(EXCEPTION_MESSAGE, null);
  }
}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.standalone.exception.MuleControllerException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class UnixControllerTest {

  private static final String MULE_HOME = "/home/mule";
  private static final int TIMEOUT = 3000;
  private final UnixController controller = new UnixController(MULE_HOME, TIMEOUT);
  private UnixController controllerSpy;
  private static String CONTROLLER_ARG = "argValue";
  private static final String[] CONTROLLER_ARGUMENTS = {CONTROLLER_ARG};
  private static final int RUNNING_STATUS = 0;
  private static final int NOT_RUNNING_STATUS = 1;

  @BeforeEach
  public void setUp() throws Exception {
    controllerSpy = spy(controller);
  }

  @Test
  public void getMuleBinTest() {
    assertThat(controller.getMuleBin()).describedAs("Mule bin path is not the expected").isEqualTo(MULE_HOME + "/bin/mule");
  }

  @Test
  public void statusRunningTest() throws Exception {
    // doReturn(RUNNING_STATUS).when(controllerSpy, "runSync", "status", CONTROLLER_ARGUMENTS);
    assertThat(controllerSpy.status(CONTROLLER_ARGUMENTS)).describedAs("Status is not the expected").isEqualTo(RUNNING_STATUS);
  }

  @Test
  public void statusNotRunningTest() throws Exception {
    // doReturn(NOT_RUNNING_STATUS).when(controllerSpy, "runSync", "status", CONTROLLER_ARGUMENTS);
    assertThat(controllerSpy.status(CONTROLLER_ARGUMENTS)).describedAs("Status is not the expected")
        .isEqualTo(NOT_RUNNING_STATUS);
  }

  @Test
  public void getProcessIdIfMuleIsRunningTest() {
    int processId = 1;
    doReturn(true).when(controllerSpy).isMuleRunning();
    doReturn(processId).when(controllerSpy).getProcessIdFromStatus();
    assertThat(controllerSpy.getProcessId()).describedAs("Process id is not the expected").isEqualTo(processId);
  }

  @Test
  public void getProcessIdMuleProcessNotRunningExceptionTest() {
    Exception exception = assertThrows(MuleControllerException.class, () -> {
      doReturn(false).when(controllerSpy).isMuleRunning();
      controllerSpy.getProcessId();
    });
    String expectedMessage = "Mule Runtime is not running";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void getProcessIdFromMuleStatusTest() throws Exception {
    int processId = 10;
    setStatusToOutputStreamInController("Mule is running (" + processId + ").");
    assertThat(controllerSpy.getProcessIdFromStatus()).describedAs("Process id is not the expected").isEqualTo(processId);
  }

  @Test
  public void getProcessIdFromMuleEEStatusTest() throws Exception {
    int processId = 10;
    setStatusToOutputStreamInController("Mule Enterprise Edition is running (" + processId + ").");
    assertThat(controllerSpy.getProcessIdFromStatus()).describedAs("Process id is not the expected").isEqualTo(processId);
  }

  @Test
  public void getProcessIdFromStatusExceptionTest() {
    Exception exception = assertThrows(MuleControllerException.class, () -> {
      setStatusToOutputStreamInController("Not running");
      controllerSpy.getProcessIdFromStatus();
    });
    String expectedMessage =
        "bin/mule status didn't return the expected pattern: Mule(\\sEnterprise Edition)? is running \\(([0-9]+)\\)\\.";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));

  }

  private void setStatusToOutputStreamInController(String status) throws Exception {
    OutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(status.getBytes(Charset.forName("UTF-8")));
    //doReturn(outputStream).when(controllerSpy).getOutputStream();
  }
}

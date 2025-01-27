/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.standalone.exception.MuleControllerException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
    assertThat(controller.getMuleBin())
        .describedAs("Mule bin path is not the expected")
        .isEqualTo(MULE_HOME + "/bin/mule");
  }

  @Test
  public void statusRunningTest() {
    doAnswer(answer -> {
      ((OutputStream) answer.getArguments()[1]).write("is running".getBytes());
      return RUNNING_STATUS;
    }).when(controllerSpy).runSync(eq("status"), any(OutputStream.class), eq(CONTROLLER_ARGUMENTS));
    assertThat(controllerSpy.status(CONTROLLER_ARGUMENTS))
        .as("Status is not the expected")
        .isEqualTo(RUNNING_STATUS);
  }

  @Test
  @Disabled
  public void statusNotRunningTest() throws Exception {
    doAnswer(answer -> {
      ((OutputStream) answer.getArguments()[1]).write("is not running".getBytes());
      return NOT_RUNNING_STATUS;
    }).when(controllerSpy).runSync(eq("status"), any(OutputStream.class), eq(CONTROLLER_ARGUMENTS));
    assertThat(controllerSpy.status(CONTROLLER_ARGUMENTS))
        .as("Status is not the expected")
        .isEqualTo(NOT_RUNNING_STATUS);
  }

  @Test
  public void getProcessIdIfMuleIsRunningTest() {
    int processId = 1;
    doReturn(true).when(controllerSpy).isMuleRunning();
    doReturn(processId).when(controllerSpy).getProcessIdFromStatus();
    assertThat(controllerSpy.getProcessId())
        .as("Process id is not the expected")
        .isEqualTo(processId);
  }

  @Test
  public void getProcessIdMuleProcessNotRunningExceptionTest() {
    doReturn(false).when(controllerSpy).isMuleRunning();
    assertThatThrownBy(() -> controllerSpy.getProcessId())
        .isExactlyInstanceOf(MuleControllerException.class)
        .hasMessage("Mule Runtime is not running");
  }

  @Test
  public void getProcessIdFromMuleStatusTest() throws Exception {
    int processId = 10;
    setStatusToOutputStreamInController("Mule is running (" + processId + ").");
    assertThat(controllerSpy.getProcessIdFromStatus())
        .as("Process id is not the expected")
        .isEqualTo(processId);
  }

  @Test
  public void getProcessIdFromMuleEEStatusTest() throws Exception {
    int processId = 10;
    setStatusToOutputStreamInController("Mule Enterprise Edition is running (" + processId + ").");
    assertThat(controllerSpy.getProcessIdFromStatus())
        .as("Process id is not the expected")
        .isEqualTo(processId);
  }

  @Test
  public void getProcessIdFromStatusExceptionTest() throws Exception {
    setStatusToOutputStreamInController("Not running");
    assertThatThrownBy(() -> controllerSpy.getProcessIdFromStatus())
        .isExactlyInstanceOf(MuleControllerException.class)
        .hasMessage("bin/mule status didn't return the expected pattern: Mule(\\sEnterprise Edition)? is running \\(([0-9]+)\\)\\.");
  }

  private void setStatusToOutputStreamInController(String status) throws Exception {
    OutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(status.getBytes(StandardCharsets.UTF_8));
    when(controllerSpy.getOutputStream()).thenReturn(outputStream);
  }

  @Test
  public void getExecutorTest() throws Exception {
    Method method = UnixController.class.getDeclaredMethod("getExecutor");
    method.setAccessible(true);

    Executor executor = (Executor) method.invoke(controllerSpy);
    assertThat(executor).isNotNull();
  }

  @Test
  public void isMuleRunningTest() {
    doReturn(0).when(controllerSpy).doExecution(any(), any(), anyMap());
    assertThat(controllerSpy.isMuleRunning()).isTrue();

    doReturn(1).when(controllerSpy).doExecution(any(), any(), anyMap());
    assertThat(controllerSpy.isMuleRunning()).isFalse();
  }

  @Test
  public void getExecuteWatchdogTest() throws Exception {
    Method method = UnixController.class.getDeclaredMethod("getExecuteWatchdog", int.class);
    method.setAccessible(true);
    ExecuteWatchdog watchdog = (ExecuteWatchdog) method.invoke(controllerSpy, 3000);
    assertThat(watchdog).isNotNull();
  }

}

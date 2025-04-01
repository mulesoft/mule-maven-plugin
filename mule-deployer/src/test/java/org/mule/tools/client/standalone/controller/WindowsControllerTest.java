/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.standalone.exception.MuleControllerException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WindowsControllerTest {

  private static final String DISPLAY_SERVICE_INFORMATION_COMMAND = "sc queryex ";
  private static final String DISPLAY_MULE_PROCESS_INFORMATION = DISPLAY_SERVICE_INFORMATION_COMMAND + "\"mule\" ";
  private static final String DISPLAY_MULE_EE_PROCESS_INFORMATION = DISPLAY_SERVICE_INFORMATION_COMMAND + "\"mule_ee\" ";
  private static final int RUNNING_STATUS = 0;
  private static final int NOT_RUNNING_STATUS = 1;

  private static final String MULE_HOME = "C:\\user\\mule";
  private static final int TIMEOUT = 3000;
  private static String CONTROLLER_ARG = "argValue";
  private static final String[] CONTROLLER_ARGUMENTS = {CONTROLLER_ARG};
  private final WindowsController controller = new WindowsController(MULE_HOME, TIMEOUT);
  private static final int RUN_SYNC_START_RETURN_VALUE = 0;
  private static final int RUN_SYNC_STOP_RETURN_VALUE = 0;
  private WindowsController controllerSpy;

  @BeforeEach
  public void setUp() throws Exception {
    controllerSpy = spy(controller);
    doReturn(RUN_SYNC_STOP_RETURN_VALUE).when(controllerSpy).runSync("stop", CONTROLLER_ARGUMENTS);
    doReturn(RUN_SYNC_START_RETURN_VALUE).when(controllerSpy).runSync("start", CONTROLLER_ARGUMENTS);
    doReturn(RUN_SYNC_START_RETURN_VALUE).when(controllerSpy).runSync("restart", CONTROLLER_ARGUMENTS);
    doNothing().when(controllerSpy).install(CONTROLLER_ARGUMENTS);
    reset(osController);
  }

  private final AbstractOSController osController = mock(AbstractOSController.class);

  @Test
  public void getMuleBinTest() {
    assertThat(controller.getMuleBin()).describedAs("Mule bin path is not the expected").isEqualTo(MULE_HOME + "/bin/mule.bat");
  }

  @Test
  public void startTest() {
    controllerSpy.start(CONTROLLER_ARGUMENTS);
    verify(controllerSpy, times(1)).install(CONTROLLER_ARGUMENTS);
    controllerSpy.install(CONTROLLER_ARG);
    verify(controllerSpy, times(1)).runSync("start", CONTROLLER_ARGUMENTS);
  }

  @Test
  public void stopSuccessfullyReturnZeroTest() {
    int runSyncRemoveSuccessReturnValue = 0;
    doReturn(runSyncRemoveSuccessReturnValue).when(controllerSpy).runSync("remove");//, "runSync", "remove");
    assertThat(controllerSpy.stop(CONTROLLER_ARGUMENTS)).describedAs("The return value is not the expected")
        .isEqualTo(RUN_SYNC_STOP_RETURN_VALUE);
  }

  @Test
  public void stopSuccessfullyReturn0x424Test() {
    int runSyncRemoveSuccessReturnValue = 0x424;
    doReturn(runSyncRemoveSuccessReturnValue).when(controllerSpy).runSync("remove");
    assertThat(controllerSpy.stop(CONTROLLER_ARGUMENTS)).describedAs("The return value is not the expected")
        .isEqualTo(RUN_SYNC_STOP_RETURN_VALUE);
  }

  @Test
  public void stopUnsuccessfullyTest() {
    assertThatThrownBy(() -> {
      int runSyncRemoveSuccessReturnValue = 1;
      doReturn(runSyncRemoveSuccessReturnValue).when(controllerSpy).runSync("remove");
      controllerSpy.stop(CONTROLLER_ARGUMENTS);
    }).isInstanceOf(MuleControllerException.class)
        .hasMessageContaining("The mule instance couldn't be removed as a service");
  }

  @Test
  public void getProcessIdMuleTest() {
    int muleProcessPID = 1234;

    doReturn("RUNNING PID   : " + muleProcessPID).when(controllerSpy).executeCmd(DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn("").when(controllerSpy).executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION);

    assertThat(controllerSpy.getProcessId()).describedAs("Process id is not the expected").isEqualTo(muleProcessPID);
  }

  @Test
  public void getProcessIdMuleEETest() {
    int muleEEProcessPID = 4321;

    doReturn("").when(controllerSpy).executeCmd(DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn("RUNNING PID   : " + muleEEProcessPID).when(controllerSpy).executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION);

    assertThat(controllerSpy.getProcessId()).describedAs("Process id is not the expected").isEqualTo(muleEEProcessPID);
  }

  @Test
  public void getProcessIdNoProcessRunningTest() {
    assertThatThrownBy(() -> {
      doReturn(StringUtils.EMPTY).when(controllerSpy).executeCmd(DISPLAY_MULE_PROCESS_INFORMATION);
      doReturn(StringUtils.EMPTY).when(controllerSpy).executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION);

      controllerSpy.getProcessId();
    }).isInstanceOf(MuleControllerException.class)
        .hasMessageContaining("No mule instance is running");
  }

  @Test
  public void getStatusMuleRunningTest() {
    doReturn("RUNNING PID   : 1").when(controllerSpy).executeCmd(DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn(StringUtils.EMPTY).when(controllerSpy).executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION);

    assertThat(controllerSpy.status(StringUtils.EMPTY)).describedAs("Status is not the expected").isEqualTo(RUNNING_STATUS);
  }

  @Test
  public void getStatusMuleEERunningTest() {
    doReturn(StringUtils.EMPTY).when(controllerSpy).executeCmd(DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn("RUNNING PID   : 1").when(controllerSpy).executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION);

    assertThat(controllerSpy.status(StringUtils.EMPTY)).describedAs("Status is not the expected").isEqualTo(RUNNING_STATUS);
  }

  @Test
  public void getStatusNoProcessesRunningTest() {
    doReturn(StringUtils.EMPTY).when(controllerSpy).executeCmd(DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn(StringUtils.EMPTY).when(controllerSpy).executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION);

    assertThat(controllerSpy.status(StringUtils.EMPTY)).describedAs("Status is not the expected").isEqualTo(NOT_RUNNING_STATUS);
  }

  @Test
  public void restartTest() {
    controllerSpy.restart(CONTROLLER_ARGUMENTS);
    controllerSpy.install(CONTROLLER_ARG);
    verify(controllerSpy, times(1)).runSync("restart", CONTROLLER_ARGUMENTS);
  }

  @Test
  public void executeCmdTest() throws Exception {
    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(errorStream));

    try {
      WindowsController controller = new WindowsController("C:\\user\\mule", 3000);
      String output = controller.executeCmd("dummy");
      assertThat(output).isEqualTo("");

      String errorOutput = errorStream.toString();
      assertThat(errorOutput)
          .contains("Cannot run program \"dummy\"")
          .containsAnyOf("No such file or directory", "The system cannot find the file specified");
    } finally {
      System.setErr(originalErr);
    }
  }

  @Test
  public void executeCmdTryTest() {
    String validCommand = "echo Hello, World!";

    WindowsController controller = new WindowsController("C:\\user\\mule", 3000);

    String output = controller.executeCmd(validCommand);
    assertThat(output.trim()).isEqualTo("Hello, World!");
  }
}

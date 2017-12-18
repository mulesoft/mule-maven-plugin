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

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WindowsController.class})
public class WindowsControllerTest {

  private static final String EXECUTE_CMD_METHOD_NAME = "executeCmd";
  private static final String DISPLAY_SERVICE_INFORMATION_COMMAND = "sc queryex ";
  private static final String DISPLAY_MULE_PROCESS_INFORMATION = DISPLAY_SERVICE_INFORMATION_COMMAND + "\"mule\" ";
  private static final String DISPLAY_MULE_EE_PROCESS_INFORMATION = DISPLAY_SERVICE_INFORMATION_COMMAND + "\"mule_ee\" ";
  private static final int RUNNING_STATUS = 0;
  private static final int NOT_RUNNING_STATUS = 1;
  @Rule
  public ExpectedException expected = ExpectedException.none();

  private static final String MULE_HOME = "C:\\user\\mule";
  private static final int TIMEOUT = 3000;
  private static String CONTROLLER_ARG = "argValue";
  private static final String[] CONTROLLER_ARGUMENTS = {CONTROLLER_ARG};
  private final WindowsController controller = new WindowsController(MULE_HOME, TIMEOUT);
  private static final int RUN_SYNC_START_RETURN_VALUE = 0;
  private static final int RUN_SYNC_STOP_RETURN_VALUE = 0;
  private WindowsController controllerSpy;

  @Before
  public void setUp() throws Exception {
    controllerSpy = spy(controller);
    doReturn(RUN_SYNC_STOP_RETURN_VALUE).when(controllerSpy, "runSync", "stop", CONTROLLER_ARGUMENTS);
    doReturn(RUN_SYNC_START_RETURN_VALUE).when(controllerSpy, "runSync", "start", CONTROLLER_ARGUMENTS);
    doReturn(RUN_SYNC_START_RETURN_VALUE).when(controllerSpy, "runSync", "restart", CONTROLLER_ARGUMENTS);
    doNothing().when(controllerSpy, "install", CONTROLLER_ARGUMENTS);
  }

  @Test
  public void getMuleBinTest() {
    assertThat("Mule bin path is not the expected", controller.getMuleBin(), equalTo(MULE_HOME + "/bin/mule.bat"));
  }

  @Test
  public void startTest() throws Exception {
    controllerSpy.start(CONTROLLER_ARGUMENTS);
    verifyPrivate(controllerSpy).invoke("install", CONTROLLER_ARG);
    verify(controllerSpy, times(1)).runSync("start", CONTROLLER_ARGUMENTS);
  }

  @Test
  public void stopSuccessfullyReturnZeroTest() throws Exception {
    int runSyncRemoveSuccessReturnValue = 0;
    doReturn(runSyncRemoveSuccessReturnValue).when(controllerSpy, "runSync", "remove");
    assertThat("The return value is not the expected", controllerSpy.stop(CONTROLLER_ARGUMENTS),
               equalTo(RUN_SYNC_STOP_RETURN_VALUE));
  }

  @Test
  public void stopSuccessfullyReturn0x424Test() throws Exception {
    int runSyncRemoveSuccessReturnValue = 0x424;
    doReturn(runSyncRemoveSuccessReturnValue).when(controllerSpy, "runSync", "remove");
    assertThat("The return value is not the expected", controllerSpy.stop(CONTROLLER_ARGUMENTS),
               equalTo(RUN_SYNC_STOP_RETURN_VALUE));
  }

  @Test
  public void stopUnsuccessfullyTest() throws Exception {
    expected.expect(MuleControllerException.class);
    expected.expectMessage("The mule instance couldn't be removed as a service");

    int runSyncRemoveSuccessReturnValue = 1;
    doReturn(runSyncRemoveSuccessReturnValue).when(controllerSpy, "runSync", "remove");
    controllerSpy.stop(CONTROLLER_ARGUMENTS);
  }

  @Test
  public void getProcessIdMuleTest() throws Exception {
    int muleProcessPID = 1234;

    doReturn("RUNNING PID   : " + muleProcessPID).when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn("").when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_EE_PROCESS_INFORMATION);

    assertThat("Process id is not the expected", controllerSpy.getProcessId(), equalTo(muleProcessPID));
  }

  @Test
  public void getProcessIdMuleEETest() throws Exception {
    int muleEEProcessPID = 4321;

    doReturn("").when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn("RUNNING PID   : " + muleEEProcessPID).when(controllerSpy, EXECUTE_CMD_METHOD_NAME,
                                                         DISPLAY_MULE_EE_PROCESS_INFORMATION);

    assertThat("Process id is not the expected", controllerSpy.getProcessId(), equalTo(muleEEProcessPID));
  }

  @Test
  public void getProcessIdNoProcessRunningTest() throws Exception {
    expected.expect(MuleControllerException.class);
    expected.expectMessage("No mule instance is running");

    doReturn(StringUtils.EMPTY).when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn(StringUtils.EMPTY).when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_EE_PROCESS_INFORMATION);

    controllerSpy.getProcessId();
  }

  @Test
  public void getStatusMuleRunningTest() throws Exception {
    doReturn("RUNNING PID   : 1").when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn(StringUtils.EMPTY).when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_EE_PROCESS_INFORMATION);

    assertThat("Status is not the expected", controllerSpy.status(StringUtils.EMPTY), equalTo(RUNNING_STATUS));
  }

  @Test
  public void getStatusMuleEERunningTest() throws Exception {
    doReturn(StringUtils.EMPTY).when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn("RUNNING PID   : 1").when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_EE_PROCESS_INFORMATION);

    assertThat("Status is not the expected", controllerSpy.status(StringUtils.EMPTY), equalTo(RUNNING_STATUS));
  }

  @Test
  public void getStatusNoProcessesRunningTest() throws Exception {
    doReturn(StringUtils.EMPTY).when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_PROCESS_INFORMATION);
    doReturn(StringUtils.EMPTY).when(controllerSpy, EXECUTE_CMD_METHOD_NAME, DISPLAY_MULE_EE_PROCESS_INFORMATION);

    assertThat("Status is not the expected", controllerSpy.status(StringUtils.EMPTY), equalTo(NOT_RUNNING_STATUS));
  }

  @Test
  public void restartTest() throws Exception {
    controllerSpy.restart(CONTROLLER_ARGUMENTS);
    verifyPrivate(controllerSpy).invoke("install", CONTROLLER_ARG);
    verify(controllerSpy, times(1)).runSync("restart", CONTROLLER_ARGUMENTS);
  }

}

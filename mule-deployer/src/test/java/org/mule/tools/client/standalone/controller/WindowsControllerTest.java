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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.tools.client.standalone.exception.MuleControllerException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
  private final TestWindowsController controller = new TestWindowsController(MULE_HOME, TIMEOUT);
  private static final int RUN_SYNC_START_RETURN_VALUE = 0;
  private static final int RUN_SYNC_STOP_RETURN_VALUE = 0;
  private TestWindowsController controllerSpy;

  @Before
  public void setUp() throws Exception {
    controllerSpy = spy(controller);
  }

  @Test
  public void getMuleBinTest() {
    assertThat("Mule bin path is not the expected", controller.getMuleBin(), equalTo(MULE_HOME + "/bin/mule.bat"));
  }

  @Test
  public void startTest() throws Exception {
    controllerSpy.start(CONTROLLER_ARGUMENTS);
    verify(controllerSpy).install(CONTROLLER_ARG);
    verify(controllerSpy, times(1)).runSync("start", CONTROLLER_ARGUMENTS);
  }

  @Test
  public void stopSuccessfullyReturnZeroTest() throws Exception {
    int runSyncRemoveSuccessReturnValue = 0;
    when(controllerSpy.runSync("remove")).thenReturn(runSyncRemoveSuccessReturnValue);
    assertThat("The return value is not the expected", controllerSpy.stop(CONTROLLER_ARGUMENTS),
               equalTo(RUN_SYNC_STOP_RETURN_VALUE));
  }

  @Test
  public void stopSuccessfullyReturn0x424Test() throws Exception {
    int runSyncRemoveSuccessReturnValue = 0x424;
    when(controllerSpy.runSync("remove")).thenReturn(runSyncRemoveSuccessReturnValue);
    assertThat("The return value is not the expected", controllerSpy.stop(CONTROLLER_ARGUMENTS),
               equalTo(RUN_SYNC_STOP_RETURN_VALUE));
  }

  @Test
  public void stopUnsuccessfullyTest() throws Exception {
    expected.expect(MuleControllerException.class);
    expected.expectMessage("The mule instance couldn't be removed as a service");

    int runSyncRemoveSuccessReturnValue = 1;
    when(controllerSpy.runSync("remove")).thenReturn(runSyncRemoveSuccessReturnValue);
    controllerSpy.stop(CONTROLLER_ARGUMENTS);
  }

  @Test
  public void getProcessIdMuleTest() throws Exception {
    int muleProcessPID = 1234;

    when(controllerSpy.executeCmd(DISPLAY_MULE_PROCESS_INFORMATION)).thenReturn("RUNNING PID   : " + muleProcessPID);
    when(controllerSpy.executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION)).thenReturn("");
    assertThat("Process id is not the expected", controllerSpy.getProcessId(), equalTo(muleProcessPID));
  }

  @Test
  public void getProcessIdMuleEETest() throws Exception {
    int muleEEProcessPID = 4321;

    when(controllerSpy.executeCmd(DISPLAY_MULE_PROCESS_INFORMATION)).thenReturn("");
    when(controllerSpy.executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION)).thenReturn("RUNNING PID   : " + muleEEProcessPID);

    assertThat("Process id is not the expected", controllerSpy.getProcessId(), equalTo(muleEEProcessPID));
  }

  @Test
  public void getProcessIdNoProcessRunningTest() throws Exception {
    expected.expect(MuleControllerException.class);
    expected.expectMessage("No mule instance is running");

    when(controllerSpy.executeCmd(DISPLAY_MULE_PROCESS_INFORMATION)).thenReturn(StringUtils.EMPTY);
    when(controllerSpy.executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION)).thenReturn(StringUtils.EMPTY);

    controllerSpy.getProcessId();
  }

  @Test
  public void getStatusMuleRunningTest() throws Exception {
    when(controllerSpy.executeCmd(DISPLAY_MULE_PROCESS_INFORMATION)).thenReturn("RUNNING PID   : 1");
    when(controllerSpy.executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION)).thenReturn(StringUtils.EMPTY);

    assertThat("Status is not the expected", controllerSpy.status(StringUtils.EMPTY), equalTo(RUNNING_STATUS));
  }

  @Test
  public void getStatusMuleEERunningTest() throws Exception {
    when(controllerSpy.executeCmd(DISPLAY_MULE_PROCESS_INFORMATION)).thenReturn(StringUtils.EMPTY);
    when(controllerSpy.executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION)).thenReturn("RUNNING PID   : 1");

    assertThat("Status is not the expected", controllerSpy.status(StringUtils.EMPTY), equalTo(RUNNING_STATUS));
  }

  @Test
  public void getStatusNoProcessesRunningTest() throws Exception {
    when(controllerSpy.executeCmd(DISPLAY_MULE_PROCESS_INFORMATION)).thenReturn(StringUtils.EMPTY);
    when(controllerSpy.executeCmd(DISPLAY_MULE_EE_PROCESS_INFORMATION)).thenReturn(StringUtils.EMPTY);

    assertThat("Status is not the expected", controllerSpy.status(StringUtils.EMPTY), equalTo(NOT_RUNNING_STATUS));
  }

  @Test
  public void restartTest() throws Exception {
    controllerSpy.restart(CONTROLLER_ARGUMENTS);
    verify(controllerSpy).install(CONTROLLER_ARG);
    verify(controllerSpy, times(1)).runSync("restart", CONTROLLER_ARGUMENTS);
  }

  protected class TestWindowsController extends WindowsController {

    public TestWindowsController(String muleHome, int timeout) {
      super(muleHome, timeout);
    }

    @Override
    protected String executeCmd(String cmd) {
      return super.executeCmd(cmd);
    }

    @Override
    protected int runSync(String command, String... args) {
      if (command.equals("start") || command.equals("restart")) {
        return RUN_SYNC_START_RETURN_VALUE;
      } else if (command.equals("stop")) {
        return RUN_SYNC_STOP_RETURN_VALUE;
      }
      return RUN_SYNC_STOP_RETURN_VALUE;
    }

    @Override
    protected void install(String... args) {
      if (!args.equals(CONTROLLER_ARGUMENTS)) {
        super.install(args);
      }
    }
  }

}

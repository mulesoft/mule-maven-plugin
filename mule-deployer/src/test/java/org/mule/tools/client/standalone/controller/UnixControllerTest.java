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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Blob;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UnixController.class})
public class UnixControllerTest {

  private static final String MULE_HOME = "/home/mule";
  private static final int TIMEOUT = 3000;
  private final UnixController controller = new UnixController(MULE_HOME, TIMEOUT);
  private UnixController controllerSpy;
  private static String CONTROLLER_ARG = "argValue";
  private static final String[] CONTROLLER_ARGUMENTS = {CONTROLLER_ARG};
  private static final int RUNNING_STATUS = 0;
  private static final int NOT_RUNNING_STATUS = 1;

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    controllerSpy = spy(controller);
  }

  @Test
  public void getMuleBinTest() {
    assertThat("Mule bin path is not the expected", controller.getMuleBin(), equalTo(MULE_HOME + "/bin/mule"));
  }

  @Test
  public void statusRunningTest() throws Exception {
    doReturn(RUNNING_STATUS).when(controllerSpy, "runSync", "status", CONTROLLER_ARGUMENTS);
    assertThat("Status is not the expected", controllerSpy.status(CONTROLLER_ARGUMENTS), equalTo(RUNNING_STATUS));
  }

  @Test
  public void statusNotRunningTest() throws Exception {
    doReturn(NOT_RUNNING_STATUS).when(controllerSpy, "runSync", "status", CONTROLLER_ARGUMENTS);
    assertThat("Status is not the expected", controllerSpy.status(CONTROLLER_ARGUMENTS), equalTo(NOT_RUNNING_STATUS));
  }

  @Test
  public void getProcessIdIfMuleIsRunningTest() {
    int processId = 1;
    doReturn(true).when(controllerSpy).isMuleRunning();
    doReturn(processId).when(controllerSpy).getProcessIdFromStatus();
    assertThat("Process id is not the expected", controllerSpy.getProcessId(), equalTo(processId));
  }

  @Test
  public void getProcessIdMuleProcessNotRunningExceptionTest() {
    expected.expect(MuleControllerException.class);
    expected.expectMessage("Mule Runtime is not running");
    doReturn(false).when(controllerSpy).isMuleRunning();
    controllerSpy.getProcessId();
  }

  @Test
  public void getProcessIdFromMuleStatusTest() throws Exception {
    int processId = 10;
    setStatusToOutputStreamInController("Mule is running (" + processId + ").");
    assertThat("Process id is not the expected", controllerSpy.getProcessIdFromStatus(), equalTo(processId));
  }

  @Test
  public void getProcessIdFromMuleEEStatusTest() throws Exception {
    int processId = 10;
    setStatusToOutputStreamInController("Mule Enterprise Edition is running (" + processId + ").");
    assertThat("Process id is not the expected", controllerSpy.getProcessIdFromStatus(), equalTo(processId));
  }

  @Test
  public void getProcessIdFromStatusExceptionTest() throws Exception {
    expected.expect(MuleControllerException.class);
    expected
        .expectMessage("bin/mule status didn't return the expected pattern: Mule(\\sEnterprise Edition)? is running \\(([0-9]+)\\)\\.");
    setStatusToOutputStreamInController("Not running");
    controllerSpy.getProcessIdFromStatus();
  }

  private void setStatusToOutputStreamInController(String status) throws Exception {
    OutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(status.getBytes(Charset.forName("UTF-8")));
    doReturn(outputStream).when(controllerSpy, "getOutputStream");
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mule.tools.client.standalone.controller.MuleProcessController.DEFAULT_TIMEOUT;
import static org.mule.tools.client.standalone.controller.MuleProcessController.MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY;

class MuleProcessControllerTest {

  private MuleProcessController muleProcessController;
  private Controller controllerMock;

  @BeforeEach
  public void setUp() {
    controllerMock = mock(Controller.class);
    muleProcessController = spy(new MuleProcessController("some/mule/home") {

      @Override
      protected Controller getController() {
        return controllerMock;
      }
    });
  }

  @Test
  void nonParseableMuleControllerTimeoutPropertyTest() {
    System.setProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY, "abc");
    MuleProcessController controller = new MuleProcessController(StringUtils.EMPTY);
    assertThat(controller.getControllerTimeout()).as("Timeout property is not the expected").isEqualTo(DEFAULT_TIMEOUT);
    System.clearProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY);
  }

  @Test
  void parseableMuleControllerTimeoutPropertyTest() {
    String validTimeout = "1234";
    System.setProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY, validTimeout);
    MuleProcessController controller = new MuleProcessController(StringUtils.EMPTY);

    int expectedTimeout = 1234;
    assertThat(controller.getControllerTimeout()).as("Timeout property is not the expected").isEqualTo(expectedTimeout);
    System.clearProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY);
  }

  @Test
  public void stopTest() {
    muleProcessController.stop("arg1", "arg2");
    verify(controllerMock).stop("arg1", "arg2");
  }

  @Test
  public void startTest() {
    muleProcessController.start("arg1", "arg2");
    verify(controllerMock).start("arg1", "arg2");
  }

  @Test
  public void statusTest() {
    when(controllerMock.status("arg1")).thenReturn(1);
    int status = muleProcessController.status("arg1");
    assertThat(status).isEqualTo(1);
  }

  @Test
  public void restartTest() {
    muleProcessController.restart("arg1", "arg2");
    verify(controllerMock).restart("arg1", "arg2");
  }

  @Test
  public void deployTest() {
    muleProcessController.deploy("some/path");
    verify(controllerMock).deploy("some/path");
  }

  @Test
  public void isDeployedTest() {
    when(controllerMock.isDeployed("appName")).thenReturn(true);
    boolean deployed = muleProcessController.isDeployed("appName");
    assertThat(deployed).isTrue();
  }

  @Test
  public void getArtifactInternalRepositoryTest() {
    File fileMock = mock(File.class);
    when(controllerMock.getArtifactInternalRepository("artifactName")).thenReturn(fileMock);
    File file = muleProcessController.getArtifactInternalRepository("artifactName");
    assertThat(file).isEqualTo(fileMock);
  }

  @Test
  public void isDomainDeployedTest() {
    when(controllerMock.isDomainDeployed("domainName")).thenReturn(true);
    boolean domainDeployed = muleProcessController.isDomainDeployed("domainName");
    assertThat(domainDeployed).isTrue();
  }

  @Test
  public void undeployTest() {
    muleProcessController.undeploy("application");
    verify(controllerMock).undeploy("application");
  }

  @Test
  public void undeployDomainTest() {
    muleProcessController.undeployDomain("domain");
    verify(controllerMock).undeployDomain("domain");
  }

  @Test
  public void installLicenseTest() {
    muleProcessController.installLicense("path");
    verify(controllerMock).installLicense("path");
  }

  @Test
  public void addLibraryTest() {
    File jarMock = mock(File.class);
    muleProcessController.addLibrary(jarMock);
    verify(controllerMock).addLibrary(jarMock);
  }

  @Test
  public void deployDomainTest() {
    muleProcessController.deployDomain("domain");
    verify(controllerMock).deployDomain("domain");
  }

  @Test
  public void getLogTest() {
    File logMock = mock(File.class);
    when(controllerMock.getLog()).thenReturn(logMock);
    File log = muleProcessController.getLog();
    assertThat(log).isEqualTo(logMock);
  }

  @Test
  public void addConfPropertyTest() {
    muleProcessController.addConfProperty("value");
    verify(controllerMock).addConfProperty("value");
  }

  @Test
  public void isRunningTest() {
    when(controllerMock.isRunning()).thenReturn(true);
    boolean running = muleProcessController.isRunning();
    assertThat(running).isTrue();
  }

  @Test
  public void getProcessIdTest() {
    when(controllerMock.getProcessId()).thenReturn(123);
    int processId = muleProcessController.getProcessId();
    assertThat(processId).isEqualTo(123);
  }

  @Test
  public void getRuntimeInternalRepositoryTest() {
    File fileMock = mock(File.class);
    when(controllerMock.getRuntimeInternalRepository()).thenReturn(fileMock);
    File file = muleProcessController.getRuntimeInternalRepository();
    assertThat(file).isEqualTo(fileMock);
  }

  @Test
  public void undeployAllTest() {
    muleProcessController.undeployAll();
    verify(controllerMock).undeployAll();
  }

  @Test
  public void uninstallLicenseTest() {
    muleProcessController.uninstallLicense();
    verify(controllerMock).uninstallLicense();
  }

  @Test
  public void getLogStringTest() {
    File logMock = mock(File.class);
    when(controllerMock.getLog("appName")).thenReturn(logMock);
    File log = muleProcessController.getLog("appName");
    assertThat(log).isEqualTo(logMock);
  }

  @Test
  public void getControllerTest() {
    Controller controller = muleProcessController.getController();
    assertThat(controller).isEqualTo(controllerMock);
  }
}

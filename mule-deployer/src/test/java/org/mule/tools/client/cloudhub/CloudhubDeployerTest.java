/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.client.cloudhub;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class CloudhubDeployerTest {

  private static final String APP1_DOMAIN = "aaa";
  private static final String APP2_DOMAIN = "bbb";
  private static final String APP3_DOMAIN = "ccc";
  CloudhubDeployer cloudhubDeployerSpy;
  private List<Application> applications;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws DeploymentException {
    cloudhubDeployerSpy = spy(new CloudhubDeployer(new CloudHubDeployment(), mock(DeployerLog.class)));
    applications = buildApplications();
    doReturn(applications).when(cloudhubDeployerSpy).getApplications();
  }

  private List<Application> buildApplications() {
    List<Application> applications = new ArrayList<>();
    Application app1 = new Application();
    app1.domain = APP1_DOMAIN;
    Application app2 = new Application();
    app2.domain = APP2_DOMAIN;
    Application app3 = new Application();
    app3.domain = APP3_DOMAIN;
    applications.add(app1);
    applications.add(app2);
    applications.add(app3);
    return applications;
  }

  @Test
  public void findApplicationFromCurrentUserTest() {
    Application actualApp = cloudhubDeployerSpy.findApplicationFromCurrentUser("bBb");
    assertThat("Cloudhub deployer findApplicationFromCurrentUser method should have returned app2", actualApp.domain,
               equalTo(APP2_DOMAIN));
  }

  @Test
  public void findApplicationFromCurrentUserNotExistentTest() {
    Application actualApp = cloudhubDeployerSpy.findApplicationFromCurrentUser("ddd");
    assertThat("Cloudhub deployer findApplicationFromCurrentUser method should have returned null", actualApp,
               nullValue());
  }

  @Test
  public void findApplicationFromCurrentUserNullAppNameTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Application name should not be blank nor null");
    cloudhubDeployerSpy.findApplicationFromCurrentUser(null);
  }

  @Test
  public void findApplicationFromCurrentUserEmptyAppNameTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Application name should not be blank nor null");
    cloudhubDeployerSpy.findApplicationFromCurrentUser(StringUtils.EMPTY);
  }

  @Test
  public void findApplicationFromCurrentUserBlankAppNameTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Application name should not be blank nor null");
    cloudhubDeployerSpy.findApplicationFromCurrentUser(StringUtils.SPACE);
  }
}

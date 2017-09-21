/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.arm;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.tools.client.arm.model.Applications;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Target;
import org.mule.tools.model.DeploymentConfiguration;

import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@Ignore
public class ArmClientTestCase {

  private static final String USERNAME = System.getProperty("username");
  private static final String PASSWORD = System.getProperty("password");
  private static final String ENVIRONMENT = "Production";
  private ArmClient armClient;
  private DeploymentConfiguration deploymentConfiguration;

  @Before
  public void setup() {
    deploymentConfiguration = new DeploymentConfiguration();
    deploymentConfiguration.setUri("https://anypoint.mulesoft.com");
    deploymentConfiguration.setUsername(USERNAME);
    deploymentConfiguration.setPassword(PASSWORD);
    deploymentConfiguration.setEnvironment(ENVIRONMENT);
    deploymentConfiguration.setBusinessGroup("");
    deploymentConfiguration.setArmInsecure(false);
    armClient = new ArmClient(deploymentConfiguration, null);
    armClient.init();
  }

  @Test
  public void getApplications() {
    Applications apps = armClient.getApplications();
    assertThat(apps, notNullValue());
  }

  @Test
  public void findEnvironmentByName() {
    Environment environment = armClient.findEnvironmentByName("Production");
    assertThat(environment.name, equalTo("Production"));
  }

  @Test(expected = RuntimeException.class)
  public void failToFindFakeEnvironment() {
    armClient.findEnvironmentByName("notProduction");
  }

  @Test
  public void findServerByName() {
    Target target = armClient.findServerByName("server-name");
    assertThat(target.name, equalTo("server-name"));
  }

  @Test(expected = RuntimeException.class)
  public void failToFindFakeTargetName() {
    armClient.findServerByName("fake-server-name");
  }

  private Callable<Boolean> appIsStarted(final int applicationId) {
    return () -> armClient.isStarted(applicationId);
  }

}

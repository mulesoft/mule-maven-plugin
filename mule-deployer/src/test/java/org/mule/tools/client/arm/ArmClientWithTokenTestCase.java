/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.arm;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.mule.tools.client.arm.model.Applications;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Target;
import org.mule.tools.model.anypoint.ArmDeployment;

@Ignore
public class ArmClientWithTokenTestCase {

  private static final String AUTH_TOKEN = System.getProperty("anypoint.authToken");

  private static final String ENVIRONMENT = "Production";
  private ArmClient armClient;
  private ArmDeployment armDeployment;

  @Before
  public void setup() {
    armDeployment = new ArmDeployment();
    armDeployment.setUri("https://anypoint.mulesoft.com");
    armDeployment.setAuthToken(AUTH_TOKEN);
    armDeployment.setEnvironment(ENVIRONMENT);
    armDeployment.setBusinessGroup("");
    armDeployment.setArmInsecure(false);
    armClient = new ArmClient(armDeployment, null);
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
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return armClient.isStarted(applicationId);
      }
    };
  }

}

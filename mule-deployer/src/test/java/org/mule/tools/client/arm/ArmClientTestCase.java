/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.arm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mule.tools.client.arm.model.Applications;
import org.mule.tools.model.anypoint.ArmDeployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

//@Disabled("I suspect that there should be a server for the requests.")
class ArmClientTestCase {
  private static final String USERNAME = System.getProperty("username");
  private static final String PASSWORD = System.getProperty("password");

  private static final String ENVIRONMENT = "Production";
  private ArmClient armClient;
  private ArmDeployment armDeployment;

  @BeforeEach
  public void setup() {
    armDeployment = new ArmDeployment();
    armDeployment.setUri("https://anypoint.mulesoft.com");
    armDeployment.setUsername(USERNAME);
    armDeployment.setPassword(PASSWORD);
    armDeployment.setEnvironment(ENVIRONMENT);
    armDeployment.setBusinessGroup("");
    armDeployment.setArmInsecure(false);
    armClient = new ArmClient(armDeployment, null);

    armClient = Mockito.spy(new ArmClient(armDeployment, null));
  }

  @Test
  void getRegistrationTokenTest() {
    String registrationToken = armClient.getRegistrationToken();
    assertThat(registrationToken).isNotNull();
  }

  @Test
  public void getApplications() {
    Applications apps = armClient.getApplications();
    assertThat(apps).isNotNull();
  }

 /* @Test
  public void findEnvironmentByNameTest() {
    Environment environment = armClient.findEnvironmentByName("Production");
    assertThat(environment.name).isEqualTo("Production");
  }*/

/*  @Test
  public void failToFindFakeEnvironmentTest() {
    assertThatThrownBy(() -> armClient.findEnvironmentByName("notProduction"))
        .isExactlyInstanceOf(RuntimeException.class);
  }*/

/*  @Test
  public void findServerByNameTest() {
    Target target = armClient.findServerByName("server-name");
    assertThat(target.name).isEqualTo("server-name");
  }*/

/*  @Test
  public void findServerGroupByNameTest() {
    Target target = armClient.findServerGroupByName("my-server-group-name");
    assertTrue(target.name.contains("/serverGroups"));
  }*/

  @Test
  public void failToFindFakeTargetNameTest() {
    assertThatThrownBy(() -> armClient.findServerByName("fake-server-name"))
        .isExactlyInstanceOf(RuntimeException.class);
  }
}

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.arm.model.Applications;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Target;
import org.mule.tools.model.anypoint.ArmDeployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Disabled("I suspect that there should be a server for the requests.")
public class ArmClientWithTokenTestCase {

  private static final String AUTH_TOKEN = System.getProperty("anypoint.authToken");

  private static final String ENVIRONMENT = "Production";
  private ArmClient armClient;
  private ArmDeployment armDeployment;

  @BeforeEach
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
    assertThat(apps).isNotNull();
  }

  @Test
  public void findEnvironmentByName() {
    Environment environment = armClient.findEnvironmentByName("Production");
    assertThat(environment.name).isEqualTo("Production");
  }

  @Test
  public void failToFindFakeEnvironment() {
    assertThatThrownBy(() -> armClient.findEnvironmentByName("notProduction")).isExactlyInstanceOf(RuntimeException.class);
  }

  @Test
  public void findServerByName() {
    Target target = armClient.findServerByName("server-name");
    assertThat(target.name).isEqualTo("server-name");
  }

  @Test
  public void failToFindFakeTargetName() {
    assertThatThrownBy(() -> armClient.findServerByName("fake-server-name")).isExactlyInstanceOf(RuntimeException.class);
  }
}

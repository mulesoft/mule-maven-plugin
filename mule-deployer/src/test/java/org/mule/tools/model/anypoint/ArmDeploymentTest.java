/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.model.TargetType;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class ArmDeploymentTest {

  private ArmDeployment deploymentSpy;

  @BeforeEach
  public void setUp() {
    deploymentSpy = spy(ArmDeployment.class);
  }

  @Test
  public void setArmDeploymentValuesIsArmInsecureSetSystemPropertyTest() throws DeploymentException {
    String isArmInsecure = "true";
    System.setProperty("arm.insecure", isArmInsecure);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.isArmInsecure().get()).describedAs("The isArmInsecure property was not resolved by system property")
        .isEqualTo(Boolean.valueOf(isArmInsecure));
    System.clearProperty("arm.insecure");
  }

  @Test
  public void setArmDeploymentValuesIsArmInsecureNotSetTest() throws DeploymentException {
    Boolean isArmInsecureDefaultValue = Boolean.FALSE;
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.isArmInsecure().get())
        .describedAs("The arm isInsecure property was not resolved to the default value").isEqualTo(isArmInsecureDefaultValue);
  }

  @Test
  public void setArmDeploymentValuesIsFailIfNotExistsSetTest() throws DeploymentException {
    Boolean isFailIfNotExistsDefaultValue = Boolean.TRUE;
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.isFailIfNotExists().get())
        .describedAs("The isFailIfNotExists property was not resolved to the default value")
        .isEqualTo(isFailIfNotExistsDefaultValue);
  }

  @Test
  public void setArmDeploymentValuesAnypointTargetSetSystemPropertyTest() throws DeploymentException {
    String anypointTarget = "target";
    System.setProperty("anypoint.target", anypointTarget);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getTarget()).describedAs("The target property was not resolved by system property")
        .isEqualTo(anypointTarget);
    System.clearProperty("anypoint.target");
  }

  @Test
  public void setArmDeploymentValuesAnypointTargetTypeSetSystemPropertyTest() throws DeploymentException {
    String anypointTargetType = "server";
    System.setProperty("anypoint.target.type", anypointTargetType);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getTargetType()).describedAs("The target type property was not resolved by system property")
        .isEqualTo(TargetType.server);
    System.clearProperty("anypoint.target.type");
  }
}

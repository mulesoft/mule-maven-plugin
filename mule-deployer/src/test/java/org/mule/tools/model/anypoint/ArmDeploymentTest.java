/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.model.TargetType;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.spy;

public class ArmDeploymentTest {

  private ArmDeployment deploymentSpy;

  @Before
  public void setUp() {
    deploymentSpy = spy(ArmDeployment.class);
  }

  @Test
  public void setArmDeploymentValuesIsArmInsecureSetSystemPropertyTest() throws DeploymentException {
    String isArmInsecure = "true";
    System.setProperty("arm.insecure", isArmInsecure);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The isArmInsecure property was not resolved by system property",
               deploymentSpy.isArmInsecure().get(), equalTo(Boolean.valueOf(isArmInsecure)));
    System.clearProperty("arm.insecure");
  }

  @Test
  public void setArmDeploymentValuesIsArmInsecureNotSetTest() throws DeploymentException {
    Boolean isArmInsecureDefaultValue = Boolean.FALSE;
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The arm isInsecure property was not resolved to the default value",
               deploymentSpy.isArmInsecure().get(), equalTo(isArmInsecureDefaultValue));
  }

  @Test
  public void setArmDeploymentValuesIsFailIfNotExistsSetTest() throws DeploymentException {
    Boolean isFailIfNotExistsDefaultValue = Boolean.TRUE;
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The isFailIfNotExists property was not resolved to the default value",
               deploymentSpy.isFailIfNotExists().get(), equalTo(isFailIfNotExistsDefaultValue));
  }

  @Test
  public void setArmDeploymentValuesAnypointTargetSetSystemPropertyTest() throws DeploymentException {
    String anypointTarget = "target";
    System.setProperty("anypoint.target", anypointTarget);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The target property was not resolved by system property",
               deploymentSpy.getTarget(), equalTo(anypointTarget));
    System.clearProperty("anypoint.target");
  }

  @Test
  public void setArmDeploymentValuesAnypointTargetTypeSetSystemPropertyTest() throws DeploymentException {
    String anypointTargetType = "server";
    System.setProperty("anypoint.target.type", anypointTargetType);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The target type property was not resolved by system property",
               deploymentSpy.getTargetType(), equalTo(TargetType.server));
    System.clearProperty("anypoint.target.type");
  }
}

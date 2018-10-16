/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.model.TargetType;
import org.mule.tools.client.core.exception.DeploymentException;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.spy;

public class ArmDeploymentTest {

  private ArmDeployment deploymentSpy;

  public ExpectedException expectedException = ExpectedException.none();

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
               deploymentSpy.isArmInsecure(), equalTo(Boolean.valueOf(isArmInsecure)));
    System.clearProperty("arm.insecure");
  }

  @Test
  public void setArmDeploymentValuesIsArmInsecureNotSetTest() throws DeploymentException {
    Boolean isArmInsecureDefaultValue = Boolean.FALSE;
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The arm isInsecure property was not resolved to the default value",
               deploymentSpy.isArmInsecure(), equalTo(isArmInsecureDefaultValue));
  }

  @Test
  public void setArmDeploymentValuesIsFailIfNotExistsSetTest() throws DeploymentException {
    Boolean isFailIfNotExistsDefaultValue = Boolean.TRUE;
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The isFailIfNotExists property was not resolved to the default value",
               deploymentSpy.isFailIfNotExists(), equalTo(isFailIfNotExistsDefaultValue));
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

  @Test(expected = DeploymentException.class)
  public void setPropertiesMuleVersionOlder3_9_0Test() throws DeploymentException {
    expectedException.expectMessage("Properties are not allowed. Mule Runtime version should be at least 3.9.0");
    deploymentSpy.setMuleVersion("3.8.0");
    deploymentSpy.setProperties(new HashMap<String, String>());
    deploymentSpy.setEnvironmentSpecificValues();
  }

  @Test
  public void setPropertiesMuleVersionEqual3_9_0Test() throws DeploymentException {
    deploymentSpy.setMuleVersion("3.9.0");
    deploymentSpy.setProperties(new HashMap<String, String>());
    deploymentSpy.setEnvironmentSpecificValues();
  }

  @Test
  public void setPropertiesMuleVersionNewer3_9_0Test() throws DeploymentException {
    deploymentSpy.setMuleVersion("3.9.1");
    deploymentSpy.setProperties(new HashMap<String, String>());
    deploymentSpy.setEnvironmentSpecificValues();
  }
}

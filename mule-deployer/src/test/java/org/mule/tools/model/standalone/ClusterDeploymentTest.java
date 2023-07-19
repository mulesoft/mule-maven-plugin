/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.model.standalone;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.spy;

public class ClusterDeploymentTest {

  private static final String MULE_HOME = "mule_home";

  private ClusterDeployment deploymentSpy;

  @Before
  public void setUp() {
    deploymentSpy = spy(ClusterDeployment.class);
  }

  @Test
  public void setClusterDeploymentValuesSizeNotSetTest() throws DeploymentException {
    System.setProperty("mule.home", MULE_HOME);

    Integer clusterDefaultSize = 2;
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The cluster size was not resolved to the default value",
               deploymentSpy.getSize(), equalTo(clusterDefaultSize));
    System.clearProperty("mule.home");

  }
}

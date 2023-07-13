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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.spy;

public class AnypointDeploymentTest {

  private AnypointDeployment deploymentSpy;

  @BeforeEach
  public void setUp() {
    deploymentSpy = spy(AnypointDeployment.class);
  }

  @Test
  public void setAnypointDeploymentValuesAnypointUriSetSystemPropertyTest() throws DeploymentException {
    String anypointUri = "www.lala.com";
    System.setProperty("anypoint.baseUri", anypointUri);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getUri()).describedAs("The anypoint baseUri was not resolved by system property")
        .isEqualTo(anypointUri);
    System.clearProperty("anypoint.baseUri");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointUriNotSetTest() throws DeploymentException {
    String anypointUriDefaultValue = "https://anypoint.mulesoft.com";
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getUri()).describedAs("The anypoint baseUri was not resolved to the default value")
        .isEqualTo(anypointUriDefaultValue);
  }

  @Test
  public void setAnypointDeploymentValuesAnypointBusinessGroupSetSystemPropertyTest() throws DeploymentException {
    String businessGroup = "business";
    System.setProperty("anypoint.businessGroup", businessGroup);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getBusinessGroup()).describedAs("The anypoint business group was not resolved by system property")
        .isEqualTo(businessGroup);
    System.clearProperty("anypoint.businessGroup");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointBusinessGroupNotSetTest() throws DeploymentException {
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getBusinessGroup().isEmpty())
        .describedAs("The anypoint baseUri was not resolved to the default value").isTrue();
  }

  @Test
  public void setAnypointDeploymentValuesAnypointEnvironmentSetSystemPropertyTest() throws DeploymentException {
    String environment = "Production";
    System.setProperty("anypoint.environment", environment);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getEnvironment()).describedAs("The anypoint environment was not resolved by system property")
        .isEqualTo(environment);
    System.clearProperty("anypoint.environment");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointPasswordSetSystemPropertyTest() throws DeploymentException {
    String password = "1234";
    System.setProperty("anypoint.password", password);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getPassword()).describedAs("The password was not resolved by system property").isEqualTo(password);
    System.clearProperty("anypoint.password");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointMavenServerSetSystemPropertyTest() throws DeploymentException {
    String mavenServer = "server";
    System.setProperty("maven.server", mavenServer);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getServer()).describedAs("The maven server was not resolved by system property")
        .isEqualTo(mavenServer);
    System.clearProperty("maven.server");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointUsernameSetSystemPropertyTest() throws DeploymentException {
    String username = "root";
    System.setProperty("anypoint.username", username);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat(deploymentSpy.getUsername()).describedAs("The username was not resolved by system property").isEqualTo(username);
    System.clearProperty("anypoint.username");
  }
}

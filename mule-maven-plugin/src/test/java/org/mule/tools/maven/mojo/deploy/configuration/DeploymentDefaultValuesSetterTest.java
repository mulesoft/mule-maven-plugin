/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo.deploy.configuration;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.model.TargetType;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.AnypointDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.model.standalone.MuleRuntimeDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class DeploymentDefaultValuesSetterTest {

  private static final String PACKAGE_FILE_NAME = "package.jar";
  private static final String MULE_HOME = "mule_home";
  private static final String APPLICATION_NAME = "package";
  private DeploymentDefaultValuesSetter deploymentDefaultValuesSetterSpy;
  private Deployment deploymentMock;
  private MavenProject projectMock;
  private MuleRuntimeDeployment muleRuntimeDeployment;
  private AnypointDeployment anypointDeployment;
  private AgentDeployment agentDeployment;
  private ArmDeployment armDeployment;
  private ClusterDeployment clusterDeployment;
  private CloudHubDeployment cloudHubDeployment;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private Deployment deployment;

  @Before
  public void setUp() {
    muleRuntimeDeployment = new StandaloneDeployment();
    muleRuntimeDeployment.setMuleHome(new File(MULE_HOME));
    deployment = new StandaloneDeployment();
    anypointDeployment = new CloudHubDeployment();
    agentDeployment = new AgentDeployment();
    clusterDeployment = new ClusterDeployment();
    clusterDeployment.setMuleHome(new File(MULE_HOME));
    armDeployment = new ArmDeployment();
    cloudHubDeployment = new CloudHubDeployment();
    projectMock = mock(MavenProject.class);
    deploymentDefaultValuesSetterSpy = spy(DeploymentDefaultValuesSetter.class);
    System.setProperty("mule.artifact", PACKAGE_FILE_NAME);
  }

  @Test
  public void setDefaultValuesOfAgentDeploymentTest() throws MojoExecutionException {
    deploymentMock = mock(AgentDeployment.class);
    doNothing().when(deploymentDefaultValuesSetterSpy).setAgentDeploymentDefaultValues((AgentDeployment) deploymentMock,
                                                                                       projectMock);
    deploymentDefaultValuesSetterSpy.setDefaultValues(deploymentMock, projectMock);
    verify(deploymentDefaultValuesSetterSpy, times(1)).setAgentDeploymentDefaultValues((AgentDeployment) deploymentMock,
                                                                                       projectMock);
  }

  @Test
  public void setDefaultValuesOfStandaloneDeploymentTest() throws MojoExecutionException {
    deploymentMock = mock(StandaloneDeployment.class);
    doNothing().when(deploymentDefaultValuesSetterSpy).setStandaloneDeploymentDefaultValues((StandaloneDeployment) deploymentMock,
                                                                                            projectMock);
    deploymentDefaultValuesSetterSpy.setDefaultValues(deploymentMock, projectMock);
    verify(deploymentDefaultValuesSetterSpy, times(1)).setStandaloneDeploymentDefaultValues((StandaloneDeployment) deploymentMock,
                                                                                            projectMock);
  }

  @Test
  public void setDefaultValuesOfClusterDeploymentTest() throws MojoExecutionException {
    deploymentMock = mock(ClusterDeployment.class);
    doNothing().when(deploymentDefaultValuesSetterSpy).setClusterDeploymentDefaultValues((ClusterDeployment) deploymentMock,
                                                                                         projectMock);
    deploymentDefaultValuesSetterSpy.setDefaultValues(deploymentMock, projectMock);
    verify(deploymentDefaultValuesSetterSpy, times(1)).setClusterDeploymentDefaultValues((ClusterDeployment) deploymentMock,
                                                                                         projectMock);
  }

  @Test
  public void setDefaultValuesOfArmDeploymentTest() throws MojoExecutionException {
    deploymentMock = mock(ArmDeployment.class);
    doNothing().when(deploymentDefaultValuesSetterSpy).setArmDeploymentDefaultValues((ArmDeployment) deploymentMock,
                                                                                     projectMock);
    deploymentDefaultValuesSetterSpy.setDefaultValues(deploymentMock, projectMock);
    verify(deploymentDefaultValuesSetterSpy, times(1)).setArmDeploymentDefaultValues((ArmDeployment) deploymentMock,
                                                                                     projectMock);
  }

  @Test
  public void setDefaultValuesOfCloudHubDeploymentTest() throws MojoExecutionException {
    deploymentMock = mock(CloudHubDeployment.class);
    doNothing().when(deploymentDefaultValuesSetterSpy).setCloudHubDeploymentDefaultValues((CloudHubDeployment) deploymentMock,
                                                                                          projectMock);
    deploymentDefaultValuesSetterSpy.setDefaultValues(deploymentMock, projectMock);
    verify(deploymentDefaultValuesSetterSpy, times(1)).setCloudHubDeploymentDefaultValues((CloudHubDeployment) deploymentMock,
                                                                                          projectMock);
  }

  @Test
  public void setBasicDeploymentValuesApplicationFileNotSetTest() throws MojoExecutionException {
    System.clearProperty("mule.artifact");
    expectedException.expect(MojoExecutionException.class);
    expectedException
        .expectMessage("Artifact to be deployed could not be found. Please set its location setting -Dmule.artifact=path/to/jar or in the deployment configuration pom element");
    deploymentDefaultValuesSetterSpy.setBasicDeploymentValues(deployment, projectMock);
  }

  @Test
  public void setBasicDeploymentValuesApplicationFileSetSystemPropertiesTest() throws MojoExecutionException {
    deploymentDefaultValuesSetterSpy.setBasicDeploymentValues(deployment, projectMock);
    assertThat("The application package jar could not be resolved by system property", deployment.getArtifact().getPath(),
               equalTo(PACKAGE_FILE_NAME));
  }

  @Test
  public void setBasicDeploymentValuesApplicationFileByMavenProjectTest() throws MojoExecutionException {
    System.clearProperty("mule.application");
    Artifact artifactMock = mock(Artifact.class);
    when(artifactMock.getFile()).thenReturn(new File(PACKAGE_FILE_NAME));
    List<Artifact> artifacts = new ArrayList<>();
    artifacts.add(artifactMock);
    when(projectMock.getAttachedArtifacts()).thenReturn(artifacts);
    deploymentDefaultValuesSetterSpy.setBasicDeploymentValues(deployment, projectMock);
    assertThat("The application package jar could not be resolved by maven project", deployment.getArtifact().getName(),
               equalTo(PACKAGE_FILE_NAME));
  }

  @Test
  public void setBasicDeploymentValuesApplicationNameSetSystemPropertiesTest() throws MojoExecutionException {
    String applicationName = "package";
    System.setProperty("mule.application.name", applicationName);
    deploymentDefaultValuesSetterSpy.setBasicDeploymentValues(deployment, projectMock);
    assertThat("The application name could not be resolved by system property", deployment.getApplicationName(),
               equalTo(applicationName));
    System.clearProperty("mule.application.name");

  }

  @Test
  public void setBasicDeploymentValuesApplicationNameSetByMavenProjectTest() throws MojoExecutionException {
    String artifactId = "artifact-id";
    when(projectMock.getArtifactId()).thenReturn(artifactId);
    deploymentDefaultValuesSetterSpy.setBasicDeploymentValues(deployment, projectMock);
    assertThat("The application application name could not be resolved by maven project", deployment.getApplicationName(),
               equalTo(artifactId));
  }

  @Test
  public void setBasicDeploymentValuesSkipSetSystemPropertiesTest() throws MojoExecutionException {
    String isSkip = "true";
    System.setProperty("mule.skip", isSkip);
    deploymentDefaultValuesSetterSpy.setBasicDeploymentValues(deployment, projectMock);
    assertThat("The skip property could not be resolved by system property", deployment.getSkip(),
               equalTo(isSkip));
    System.clearProperty("mule.skip");

  }

  @Test
  public void setBasicDeploymentValueSkipNotSetTest() throws MojoExecutionException {
    deploymentDefaultValuesSetterSpy.setBasicDeploymentValues(deployment, projectMock);
    assertThat("The skip property could not be resolved by default", deployment.getSkip(),
               equalTo("false"));
  }

  @Test
  public void setBasicDeploymentValuesMuleVersionSetSystemPropertiesTest() throws MojoExecutionException {
    String muleVersion = "4.0.0";
    System.setProperty("mule.version", muleVersion);
    deploymentDefaultValuesSetterSpy.setBasicDeploymentValues(deployment, projectMock);
    assertThat("The mule version property could not be resolved by system property", deployment.getMuleVersion().get(),
               equalTo(muleVersion));
    System.clearProperty("mule.version");

  }

  @Test
  public void setBasicDeploymentValueMuleVersionNotSetTest() throws MojoExecutionException {
    deploymentDefaultValuesSetterSpy.setBasicDeploymentValues(deployment, projectMock);
    assertThat("The mule version should not be present", deployment.getMuleVersion().isPresent(),
               equalTo(false));
  }

  @Test
  public void setMuleRuntimeDeploymentValuesMuleHomeSetSystemPropertiesTest() throws MojoExecutionException {
    muleRuntimeDeployment.setMuleHome(null);
    System.setProperty("mule.home", MULE_HOME);
    muleRuntimeDeployment.setArtifact(new File(APPLICATION_NAME));
    deploymentDefaultValuesSetterSpy.setMuleRuntimeDeploymentValues(muleRuntimeDeployment, projectMock);
    assertThat("The mule home was not resolved by system property",
               muleRuntimeDeployment.getMuleHome().getName(), equalTo(MULE_HOME));
    System.clearProperty("mule.home");
  }

  @Test
  public void setMuleRuntimeDeploymentValuesTimeoutSetSystemPropertiesTest() throws MojoExecutionException {
    String timeout = "1000";
    System.setProperty("mule.timeout", timeout);
    deploymentDefaultValuesSetterSpy.setMuleRuntimeDeploymentValues(muleRuntimeDeployment, projectMock);
    assertThat("The timeout was not resolved by system property",
               muleRuntimeDeployment.getTimeout(), equalTo(Integer.valueOf(timeout)));
    System.clearProperty("mule.timeout");
  }

  @Test
  public void setMuleRuntimeDeploymentValuesDeploymentTimeoutSetSystemPropertiesTest() throws MojoExecutionException {
    String deploymentTimeout = "1000";
    System.setProperty("mule.deploymentConfiguration.timeout", deploymentTimeout);
    deploymentDefaultValuesSetterSpy.setMuleRuntimeDeploymentValues(muleRuntimeDeployment, projectMock);
    assertThat("The deployment timeout was not resolved by system property",
               muleRuntimeDeployment.getDeploymentTimeout(), equalTo(Long.valueOf(deploymentTimeout)));
    System.clearProperty("mule.deploymentConfiguration.timeout");
  }

  @Test
  public void setMuleRuntimeDeploymentValuesDeploymentTimeoutNotSetTest() throws MojoExecutionException {
    String deploymentTimeoutDefaultValue = "60000";
    deploymentDefaultValuesSetterSpy.setMuleRuntimeDeploymentValues(muleRuntimeDeployment, projectMock);
    assertThat("The deployment timeout was not resolved by system property",
               muleRuntimeDeployment.getDeploymentTimeout(), equalTo(Long.valueOf(deploymentTimeoutDefaultValue)));
  }

  @Test
  public void setMuleRuntimeDeploymentValuesArgumentsSetSystemPropertiesTest() throws MojoExecutionException {
    String arguments = "a,b,c";
    System.setProperty("mule.arguments", arguments);
    deploymentDefaultValuesSetterSpy.setMuleRuntimeDeploymentValues(muleRuntimeDeployment, projectMock);
    String[] resolvedArguments = muleRuntimeDeployment.getArguments();
    assertThat("The arguments array was not resolved by system property",
               asList(resolvedArguments), containsInAnyOrder("a", "b", "c"));
    System.clearProperty("mule.arguments");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointUriSetSystemPropertyTest() throws MojoExecutionException {
    String anypointUri = "www.lala.com";
    System.setProperty("anypoint.uri", anypointUri);
    deploymentDefaultValuesSetterSpy.setAnypointDeploymentValues(anypointDeployment, projectMock);
    assertThat("The anypoint uri was not resolved by system property",
               anypointDeployment.getUri(), equalTo(anypointUri));
    System.clearProperty("anypoint.uri");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointUriNotSetTest() throws MojoExecutionException {
    String anypointUriDefaultValue = "https://anypoint.mulesoft.com";
    deploymentDefaultValuesSetterSpy.setAnypointDeploymentValues(anypointDeployment, projectMock);
    assertThat("The anypoint uri was not resolved to the default value",
               anypointDeployment.getUri(), equalTo(anypointUriDefaultValue));
  }

  @Test
  public void setAnypointDeploymentValuesAnypointBusinessGroupSetSystemPropertyTest() throws MojoExecutionException {
    String businessGroup = "business";
    System.setProperty("anypoint.businessGroup", businessGroup);
    deploymentDefaultValuesSetterSpy.setAnypointDeploymentValues(anypointDeployment, projectMock);
    assertThat("The anypoint business group was not resolved by system property",
               anypointDeployment.getBusinessGroup(), equalTo(businessGroup));
    System.clearProperty("anypoint.businessGroup");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointBusinessGroupNotSetTest() throws MojoExecutionException {
    deploymentDefaultValuesSetterSpy.setAnypointDeploymentValues(anypointDeployment, projectMock);
    assertThat("The anypoint uri was not resolved to the default value",
               anypointDeployment.getBusinessGroup().isEmpty(), equalTo(true));
  }

  @Test
  public void setAnypointDeploymentValuesAnypointEnvironmentSetSystemPropertyTest() throws MojoExecutionException {
    String environment = "Production";
    System.setProperty("anypoint.environment", environment);
    deploymentDefaultValuesSetterSpy.setAnypointDeploymentValues(anypointDeployment, projectMock);
    assertThat("The anypoint environment was not resolved by system property",
               anypointDeployment.getEnvironment(), equalTo(environment));
    System.clearProperty("anypoint.environment");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointPasswordSetSystemPropertyTest() throws MojoExecutionException {
    String password = "1234";
    System.setProperty("anypoint.password", password);
    deploymentDefaultValuesSetterSpy.setAnypointDeploymentValues(anypointDeployment, projectMock);
    assertThat("The password was not resolved by system property",
               anypointDeployment.getPassword(), equalTo(password));
    System.clearProperty("anypoint.password");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointMavenServerSetSystemPropertyTest() throws MojoExecutionException {
    String mavenServer = "server";
    System.setProperty("maven.server", mavenServer);
    deploymentDefaultValuesSetterSpy.setAnypointDeploymentValues(anypointDeployment, projectMock);
    assertThat("The maven server was not resolved by system property",
               anypointDeployment.getServer(), equalTo(mavenServer));
    System.clearProperty("maven.server");
  }

  @Test
  public void setAnypointDeploymentValuesAnypointUsernameSetSystemPropertyTest() throws MojoExecutionException {
    String username = "root";
    System.setProperty("anypoint.username", username);
    deploymentDefaultValuesSetterSpy.setAnypointDeploymentValues(anypointDeployment, projectMock);
    assertThat("The username was not resolved by system property",
               anypointDeployment.getUsername(), equalTo(username));
    System.clearProperty("anypoint.username");
  }

  @Test
  public void setAgentDeploymentValuesAnypointUriSetSystemPropertyTest() throws MojoExecutionException {
    String anypointUri = "www.lala.com";
    System.setProperty("anypoint.uri", anypointUri);
    deploymentDefaultValuesSetterSpy.setAgentDeploymentDefaultValues(agentDeployment, projectMock);
    assertThat("The anypoint uri was not resolved by system property",
               agentDeployment.getUri(), equalTo(anypointUri));
    System.clearProperty("anypoint.uri");
  }

  @Test
  public void setAgentDeploymentValuesAnypointUriNotSetTest() throws MojoExecutionException {
    String anypointUriDefaultValue = "https://anypoint.mulesoft.com";
    deploymentDefaultValuesSetterSpy.setAgentDeploymentDefaultValues(agentDeployment, projectMock);
    assertThat("The anypoint uri was not resolved to the default value",
               agentDeployment.getUri(), equalTo(anypointUriDefaultValue));
  }

  @Test
  public void setClusterDeploymentValuesSizeNotSetTest() throws MojoExecutionException {
    Integer clusterDefaultSize = 2;
    deploymentDefaultValuesSetterSpy.setClusterDeploymentDefaultValues(clusterDeployment, projectMock);
    assertThat("The cluster size was not resolved to the default value",
               clusterDeployment.getSize(), equalTo(clusterDefaultSize));
  }

  @Test
  public void setArmDeploymentValuesIsArmInsecureSetSystemPropertyTest() throws MojoExecutionException {
    String isArmInsecure = "true";
    System.setProperty("arm.insecure", isArmInsecure);
    deploymentDefaultValuesSetterSpy.setArmDeploymentDefaultValues(armDeployment, projectMock);
    assertThat("The isArmInsecure property was not resolved by system property",
               armDeployment.isArmInsecure().get(), equalTo(Boolean.valueOf(isArmInsecure)));
    System.clearProperty("arm.insecure");
  }

  @Test
  public void setArmDeploymentValuesIsArmInsecureNotSetTest() throws MojoExecutionException {
    Boolean isArmInsecureDefaultValue = Boolean.FALSE;
    deploymentDefaultValuesSetterSpy.setArmDeploymentDefaultValues(armDeployment, projectMock);
    assertThat("The arm isInsecure property was not resolved to the default value",
               armDeployment.isArmInsecure().get(), equalTo(isArmInsecureDefaultValue));
  }

  @Test
  public void setArmDeploymentValuesIsFailIfNotExistsSetTest() throws MojoExecutionException {
    Boolean isFailIfNotExistsDefaultValue = Boolean.TRUE;
    deploymentDefaultValuesSetterSpy.setArmDeploymentDefaultValues(armDeployment, projectMock);
    assertThat("The isFailIfNotExists property was not resolved to the default value",
               armDeployment.isFailIfNotExists().get(), equalTo(isFailIfNotExistsDefaultValue));
  }

  @Test
  public void setArmDeploymentValuesAnypointTargetSetSystemPropertyTest() throws MojoExecutionException {
    String anypointTarget = "target";
    System.setProperty("anypoint.target", anypointTarget);
    deploymentDefaultValuesSetterSpy.setArmDeploymentDefaultValues(armDeployment, projectMock);
    assertThat("The target property was not resolved by system property",
               armDeployment.getTarget(), equalTo(anypointTarget));
    System.clearProperty("anypoint.target");
  }

  @Test
  public void setArmDeploymentValuesAnypointTargetTypeSetSystemPropertyTest() throws MojoExecutionException {
    String anypointTargetType = "server";
    System.setProperty("anypoint.target.type", anypointTargetType);
    deploymentDefaultValuesSetterSpy.setArmDeploymentDefaultValues(armDeployment, projectMock);
    assertThat("The target type property was not resolved by system property",
               armDeployment.getTargetType(), equalTo(TargetType.server));
    System.clearProperty("anypoint.target.type");
  }

  @Test
  public void setCloudHubDeploymentDefaultValuesCloudHubWorkersSetSystemPropertyTest() throws MojoExecutionException {
    String cloudHubWorkers = "10";
    System.setProperty("cloudhub.workers", cloudHubWorkers);
    deploymentDefaultValuesSetterSpy.setCloudHubDeploymentDefaultValues(cloudHubDeployment, projectMock);
    assertThat("The cloudhub workers was not resolved by system property",
               cloudHubDeployment.getWorkers().get(), equalTo(Integer.valueOf(cloudHubWorkers)));
    System.clearProperty("cloudhub.workers");
  }

  @Test
  public void setCloudHubDeploymentDefaultValuesCloudHubWorkersNotSetTest() throws MojoExecutionException {
    String cloudHubWorkersDefaultValue = "1";
    deploymentDefaultValuesSetterSpy.setCloudHubDeploymentDefaultValues(cloudHubDeployment, projectMock);
    assertThat("The cloudhub workers property was not resolved to the default value",
               cloudHubDeployment.getWorkers().get(), equalTo(Integer.valueOf(cloudHubWorkersDefaultValue)));
  }

  @Test
  public void setCloudHubDeploymentDefaultValuesCloudHubWorkerTypeSetSystemPropertyTest() throws MojoExecutionException {
    String cloudHubWorkerType = "worker-type";
    System.setProperty("cloudhub.workerType", cloudHubWorkerType);
    deploymentDefaultValuesSetterSpy.setCloudHubDeploymentDefaultValues(cloudHubDeployment, projectMock);
    assertThat("The cloudhub worker type property was not resolved by system property",
               cloudHubDeployment.getWorkerType(), equalTo(cloudHubWorkerType));
    System.clearProperty("cloudhub.workerType");
  }

  @Test
  public void setCloudHubDeploymentDefaultValuesCloudHubWorkerTypeNotSetTest() throws MojoExecutionException {
    String cloudHubWorkerTypeDefaultValue = "Medium";
    deploymentDefaultValuesSetterSpy.setCloudHubDeploymentDefaultValues(cloudHubDeployment, projectMock);
    assertThat("The cloudhub worker type property was not resolved to the default value",
               cloudHubDeployment.getWorkerType(), equalTo(cloudHubWorkerTypeDefaultValue));
  }
}

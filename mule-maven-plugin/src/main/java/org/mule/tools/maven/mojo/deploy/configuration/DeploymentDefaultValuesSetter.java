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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
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

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DeploymentDefaultValuesSetter {

  public void setDefaultValues(Deployment deployment, MavenProject project) throws MojoExecutionException {
    if (deployment instanceof AgentDeployment) {
      setAgentDeploymentDefaultValues((AgentDeployment) deployment, project);
    } else if (deployment instanceof StandaloneDeployment) {
      setStandaloneDeploymentDefaultValues((StandaloneDeployment) deployment, project);
    } else if (deployment instanceof ClusterDeployment) {
      setClusterDeploymentDefaultValues((ClusterDeployment) deployment, project);
    } else if (deployment instanceof CloudHubDeployment) {
      setCloudHubDeploymentDefaultValues((CloudHubDeployment) deployment, project);
    } else if (deployment instanceof ArmDeployment) {
      setArmDeploymentDefaultValues((ArmDeployment) deployment, project);
    }
  }


  protected void setBasicDeploymentValues(Deployment deployment, MavenProject project) throws MojoExecutionException {

    String muleApplicationName = getProperty("mule.application.name");
    if (isNotBlank(muleApplicationName)) {
      deployment.setApplicationName(muleApplicationName);
    }
    if (isBlank(deployment.getApplicationName())) {
      deployment.setApplicationName(project.getArtifactId());
    }

    String isSkip = getProperty("mule.skip");
    if (isNotBlank(isSkip)) {
      deployment.setSkip(isSkip);
    }
    if (isBlank(deployment.getSkip())) {
      deployment.setSkip("false");
    }

    String muleVersion = getProperty("mule.version");
    if (isNotBlank(muleVersion)) {
      deployment.setMuleVersion(muleVersion);
    }

    String applicationPath = getProperty("mule.application");
    if (isNotBlank(applicationPath)) {
      deployment.setApplication(new File(applicationPath));
    }
    if (deployment.getApplication() == null) {
      if (project.getAttachedArtifacts().isEmpty()) {
        throw new MojoExecutionException("Package to be deployed could not be found. Please set its location setting -Dmule.application=path/to/jar or in the deployment configuration pom element");
      }
      deployment.setApplication(project.getAttachedArtifacts().get(0).getFile());
    }
  }

  protected void setMuleRuntimeDeploymentValues(MuleRuntimeDeployment deployment, MavenProject project)
      throws MojoExecutionException {
    setBasicDeploymentValues(deployment, project);

    String scriptLocation = getProperty("mule.script");
    if (isNotBlank(scriptLocation)) {
      deployment.setScript(new File(scriptLocation));
    }

    String timeout = getProperty("mule.timeout");
    if (isNotBlank(timeout)) {
      deployment.setTimeout(Integer.valueOf(timeout));
    }

    String deploymentTimeout = getProperty("mule.deploymentConfiguration.timeout");
    if (isNotBlank(deploymentTimeout)) {
      deployment.setDeploymentTimeout(Long.valueOf(deploymentTimeout));
    }
    if (deployment.getDeploymentTimeout() == null) {
      deployment.setDeploymentTimeout(60000L);
    }

    String arguments = getProperty("mule.arguments");
    if (isNotBlank(arguments)) {
      deployment.setArguments(arguments.split(","));
    }

    String muleHome = getProperty("mule.home");
    if (isNotBlank(muleHome)) {
      deployment.setMuleHome(new File(muleHome));
    }
    if (deployment.getMuleHome() == null) {
      throw new MojoExecutionException("Invalid deployment configuration, missing mule home value. Please set it either through the plugin configuration or -Dmule.home when building the current project");
    }
  }

  protected void setAnypointDeploymentValues(AnypointDeployment deployment, MavenProject project) throws MojoExecutionException {
    setBasicDeploymentValues(deployment, project);

    String anypointUri = getProperty("anypoint.uri");
    if (isNotBlank(anypointUri)) {
      deployment.setUri(anypointUri);
    }
    if (isBlank(deployment.getUri())) {
      deployment.setUri("https://anypoint.mulesoft.com");
    }

    String businessGroup = getProperty("anypoint.businessGroup");
    if (isNotBlank(businessGroup)) {
      deployment.setBusinessGroup(businessGroup);
    }
    if (isBlank(deployment.getBusinessGroup())) {
      deployment.setBusinessGroup(StringUtils.EMPTY);
    }

    String anypointEnvironment = getProperty("anypoint.environment");
    if (isNotBlank(anypointEnvironment)) {
      deployment.setEnvironment(anypointEnvironment);
    }

    String password = getProperty("anypoint.password");
    if (isNotBlank(password)) {
      deployment.setPassword(password);
    }

    String mavenServer = getProperty("maven.server");
    if (isNotBlank(mavenServer)) {
      deployment.setServer(mavenServer);
    }

    String username = getProperty("anypoint.username");
    if (isNotBlank(username)) {
      deployment.setUsername(username);
    }
  }


  protected void setAgentDeploymentDefaultValues(AgentDeployment deployment, MavenProject project) throws MojoExecutionException {
    setBasicDeploymentValues(deployment, project);

    String anypointUri = getProperty("anypoint.uri");
    if (isNotBlank(anypointUri)) {
      deployment.setUri(anypointUri);
    }
    if (isBlank(deployment.getUri())) {
      deployment.setUri("https://anypoint.mulesoft.com");
    }
  }

  protected void setStandaloneDeploymentDefaultValues(StandaloneDeployment deployment, MavenProject project)
      throws MojoExecutionException {
    setMuleRuntimeDeploymentValues(deployment, project);
  }

  protected void setClusterDeploymentDefaultValues(ClusterDeployment deployment, MavenProject project)
      throws MojoExecutionException {
    setMuleRuntimeDeploymentValues(deployment, project);

    if (deployment.getSize() == null) {
      deployment.setSize(2);
    }
  }

  protected void setArmDeploymentDefaultValues(ArmDeployment deployment, MavenProject project) throws MojoExecutionException {
    setAnypointDeploymentValues(deployment, project);

    String isArmInsecure = getProperty("arm.insecure");
    if (isNotBlank(isArmInsecure)) {
      deployment.setArmInsecure(Boolean.valueOf(isArmInsecure));
    }
    if (!deployment.isArmInsecure().isPresent()) {
      deployment.setArmInsecure(false);
    }

    if (!deployment.isFailIfNotExists().isPresent()) {
      deployment.setFailIfNotExists(Boolean.TRUE);
    }

    String anypointTarget = getProperty("anypoint.target");
    if (isNotBlank(anypointTarget)) {
      deployment.setTarget(anypointTarget);
    }

    String targetType = getProperty("anypoint.target.type");
    if (isNotBlank(targetType)) {
      deployment.setTargetType(TargetType.valueOf(targetType));
    }
  }

  protected void setCloudHubDeploymentDefaultValues(CloudHubDeployment deployment, MavenProject project)
      throws MojoExecutionException {
    setAnypointDeploymentValues(deployment, project);

    String cloudHubWorkers = getProperty("cloudhub.workers");
    if (isNotBlank(cloudHubWorkers)) {
      deployment.setWorkers(Integer.valueOf(cloudHubWorkers));
    }
    if (!deployment.getWorkers().isPresent()) {
      deployment.setWorkers(Integer.valueOf("1"));
    }

    String cloudHubWorkerType = getProperty("cloudhub.workerType");
    if (isNotBlank(cloudHubWorkerType)) {
      deployment.setWorkerType(cloudHubWorkerType);
    }
    if (isBlank(deployment.getWorkerType())) {
      deployment.setWorkerType("Medium");
    }

    if (isBlank(deployment.getRegion())) {
      deployment.setRegion(getProperty("cloudhub.region", "us-east-1"));
    }
  }
}

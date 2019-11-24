/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.validation.project.ProjectRequirement;
import org.mule.tools.api.verifier.ProjectVerifier;
import org.mule.tools.api.verifier.ProjectVerifyFactory;
import org.mule.tools.api.repository.MuleMavenPluginClientBuilder;
import org.mule.tools.api.validation.project.AbstractProjectValidator;
import org.mule.tools.api.validation.project.ProjectValidatorFactory;
import org.mule.tools.api.classloader.model.resolver.Plugin;
import org.mule.tools.api.util.ArtifactUtils;
import org.mule.tools.api.util.MavenPackagerLog;
import org.mule.tools.api.util.MavenProjectInformation;
import org.mule.tools.maven.utils.ProjectDirectoryUpdater;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.repository.RemoteRepository;

public abstract class AbstractGenericMojo extends AbstractMojo {

  @Parameter
  protected CloudHubDeployment cloudHubDeployment;

  @Parameter
  protected ArmDeployment armDeployment;

  @Parameter
  protected StandaloneDeployment standaloneDeployment;

  @Parameter
  protected AgentDeployment agentDeployment;

  @Parameter
  protected RuntimeFabricDeployment runtimeFabricDeployment;

  @Parameter
  protected ClusterDeployment clusterDeployment;

  @Parameter(readonly = true, required = true, defaultValue = "${localRepository}")
  protected ArtifactRepository localRepository;

  @Parameter(readonly = true, required = true, defaultValue = "${session}")
  protected MavenSession session;

  @Parameter(property = "project", required = true)
  protected MavenProject project;

  @Parameter(property = "shared.libraries")
  protected List<SharedLibraryDependency> sharedLibraries;

  @Parameter
  protected List<Plugin> additionalPluginDependencies;

  @Parameter(defaultValue = "${strictCheck}")
  protected boolean strictCheck;

  @Parameter(defaultValue = "${disableSemver}")
  protected boolean disableSemver;

  @Parameter(defaultValue = "${project.basedir}")
  protected File projectBaseFolder;

  @Parameter(defaultValue = "${projectBuildDirectory}")
  protected String projectBuildDirectory;

  @Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
  protected List<ArtifactRepository> remoteArtifactRepositories;

  @Parameter(defaultValue = "${testJar}")
  protected boolean testJar = false;

  @Parameter
  protected String classifier;

  @Parameter(defaultValue = "${lightweightPackage}")
  protected boolean lightweightPackage = false;

  @Parameter(defaultValue = "${useLocalRepository}")
  protected boolean useLocalRepository = false;

  protected AbstractProjectValidator validator;

  protected ProjectVerifier verifier;

  protected AetherMavenClient aetherMavenClient;

  public abstract String getPreviousRunPlaceholder();

  public abstract void doExecute() throws MojoExecutionException, MojoFailureException;

  public void initMojo() {
    if (projectBuildDirectory != null) {
      new ProjectDirectoryUpdater(project).updateBuildDirectory(projectBuildDirectory);
    }
    if (!lightweightPackage && useLocalRepository) {
      getLog().info("'useLocalRepository' would be ignored, it can only be used when 'lightweightPackage' is enabled");
      useLocalRepository = false;
    }
  }

  public void setCloudHubDeployment(CloudHubDeployment cloudHubDeployment) {
    this.cloudHubDeployment = cloudHubDeployment;
  }

  public void setArmDeployment(ArmDeployment armDeployment) {
    this.armDeployment = armDeployment;
  }

  public void setStandaloneDeployment(StandaloneDeployment standaloneDeployment) {
    this.standaloneDeployment = standaloneDeployment;
  }

  public void setAgentDeployment(AgentDeployment agentDeployment) {
    this.agentDeployment = agentDeployment;
  }

  public void setClusterDeployment(ClusterDeployment clusterDeployment) {
    this.clusterDeployment = clusterDeployment;
  }

  public void setRuntimeFabricDeployment(RuntimeFabricDeployment runtimeFabricDeployment) {
    this.runtimeFabricDeployment = runtimeFabricDeployment;
  }

  public void setSession(MavenSession session) {
    this.session = session;
  }

  public void setProject(MavenProject project) {
    this.project = project;
  }

  public void setProjectBaseFolder(File projectBaseFolder) {
    this.projectBaseFolder = projectBaseFolder;
  }

  public ProjectVerifier getProjectVerifier() {
    if (verifier == null) {
      verifier =
          ProjectVerifyFactory.create(getProjectInformation());
    }
    return verifier;
  }

  public AbstractProjectValidator getProjectValidator() {
    if (validator == null) {
      ProjectRequirement requirement = new ProjectRequirement.ProjectRequirementBuilder().withStrictCheck(strictCheck)
          .withDisableSemver(disableSemver).build();
      validator =
          ProjectValidatorFactory.create(getProjectInformation(), getAetherMavenClient(), sharedLibraries, requirement);
    }
    return validator;
  }

  protected AetherMavenClient getAetherMavenClient() {
    if (aetherMavenClient == null) {
      MavenExecutionRequest request = session.getRequest();
      List<RemoteRepository> remoteRepositories = RepositoryUtils.toRepos(remoteArtifactRepositories);
      aetherMavenClient = new MuleMavenPluginClientBuilder(new MavenPackagerLog(getLog()))
          .withRemoteRepositories(remoteRepositories)
          .withLocalRepository(request.getLocalRepositoryPath())
          .withUserSettings(request.getUserSettingsFile())
          .withGlobalSettings(request.getGlobalSettingsFile())
          .withUserProperties(request.getUserProperties())
          .withActiveProfiles(request.getActiveProfiles())
          .withInactiveProfiles(request.getInactiveProfiles())
          .build();
    }
    return aetherMavenClient;
  }

  protected List<ArtifactCoordinates> toArtifactCoordinates(List<Dependency> dependencies) {
    return dependencies.stream().map(ArtifactUtils::toArtifactCoordinates).collect(Collectors.toList());
  }


  protected ProjectInformation getProjectInformation() {
    return MavenProjectInformation.getProjectInformation(session, project, projectBaseFolder, testJar, getDeployments(),
                                                         getClassifier());
  }

  /**
   * This method avoids running a Mojo more than once.
   *
   * @return true if the Mojo run has already happened before
   */
  protected boolean hasExecutedBefore() {
    Map<?, ?> pluginContext = getPluginContext();
    if (pluginContext.containsKey(getPreviousRunPlaceholder())) {
      return true;
    }
    getPluginContext().put(getPreviousRunPlaceholder(), getPreviousRunPlaceholder());
    return false;
  }


  protected PackagingType getPackagingType() {
    return PackagingType.fromString(project.getPackaging());
  }

  public String getClassifier() {
    return getPackagingType().resolveClassifier(classifier, lightweightPackage, testJar);
  }

  public List<Deployment> getDeployments() {
    return Arrays.asList(cloudHubDeployment, clusterDeployment, agentDeployment, armDeployment, standaloneDeployment,
                         runtimeFabricDeployment);
  }
}

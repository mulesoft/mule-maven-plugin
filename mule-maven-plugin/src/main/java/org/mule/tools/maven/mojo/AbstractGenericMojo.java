/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import java.io.File;
//import java.nio.file.Paths;
//import java.util.Arrays;
import java.util.Map;

//import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
//import org.apache.maven.model.DeploymentRepository;
//import org.apache.maven.model.DistributionManagement;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
//import org.apache.maven.settings.Settings;
//import org.eclipse.aether.repository.RemoteRepository;
//import org.mule.maven.client.internal.AetherMavenClient;
//import org.mule.tools.api.classloader.model.SharedLibraryDependency;
//import org.mule.tools.api.packager.packaging.PackagingType;
//import org.mule.tools.api.repository.MuleMavenPluginClientBuilder;
//import org.mule.tools.api.validation.exchange.ExchangeRepositoryMetadata;
//import org.mule.tools.api.validation.project.AbstractProjectValidator;
//import org.mule.tools.api.validation.project.ProjectValidatorFactory;
//import org.mule.tools.client.authentication.model.Credentials;
//import org.mule.tools.maven.util.ArtifactUtils;
//import org.mule.tools.maven.util.DependencyProject;
//import org.mule.tools.maven.util.MavenPackagerLog;
//import org.mule.tools.maven.util.ProjectDirectoryUpdater;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.standalone.ClusterDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;

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
  protected ClusterDeployment clusterDeployment;


  @Parameter(readonly = true, required = true, defaultValue = "${localRepository}")
  protected ArtifactRepository localRepository;

  @Parameter(readonly = true, required = true, defaultValue = "${session}")
  protected MavenSession session;

  @Parameter(property = "project", required = true)
  protected MavenProject project;

  // mule-packager

  // @Parameter(property = "shared.libraries")
  // protected List<SharedLibraryDependency> sharedLibraries;

  // mule-packager

  // @Parameter(defaultValue = "${strictCheck}")
  // protected boolean strictCheck;

  @Parameter(defaultValue = "${project.basedir}")
  protected File projectBaseFolder;

  @Parameter(defaultValue = "${projectBuildDirectory}")
  protected String projectBuildDirectory;

  // mule-packager

  // @Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
  // protected List<ArtifactRepository> remoteArtifactRepositories;

  // mule-packager

  // @Parameter(defaultValue = "${testJar}")
  // protected boolean testJar = false;

  @Parameter
  protected String classifier;

  // mule-packager

  // @Parameter(defaultValue = "${lightweightPackage}")
  // protected boolean lightweightPackage = false;

  // mule-packager

  // protected AbstractProjectValidator validator;

  // Used mainly for repository generation in 3.x

  // protected AetherMavenClient aetherMavenClient;

  // private ProjectInformation projectInformation;

  public abstract String getPreviousRunPlaceholder();

  public abstract void doExecute() throws MojoExecutionException, MojoFailureException;

  // mule-packager

  // public void initMojo() {
  // if (projectBuildDirectory != null) {
  // new ProjectDirectoryUpdater(project).updateBuildDirectory(projectBuildDirectory);
  // }
  // }

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

  public void setSession(MavenSession session) {
    this.session = session;
  }

  public void setProject(MavenProject project) {
    this.project = project;
  }

  // mule-packager

  // public void setProjectBaseFolder(File projectBaseFolder) {
  // this.projectBaseFolder = projectBaseFolder;
  // }

  // mule-packager

  // public AbstractProjectValidator getProjectValidator() {
  // if (validator == null) {
  // validator =
  // ProjectValidatorFactory.create(getAndSetProjectInformation(), getAetherMavenClient(), sharedLibraries, strictCheck);
  // }
  // return validator;
  // }

  // mule-packager

  // protected AetherMavenClient getAetherMavenClient() {
  // if (aetherMavenClient == null) {
  // MavenExecutionRequest request = session.getRequest();
  // List<RemoteRepository> remoteRepositories = RepositoryUtils.toRepos(remoteArtifactRepositories);
  // aetherMavenClient = new MuleMavenPluginClientBuilder(new MavenPackagerLog(getLog()))
  // .withRemoteRepositories(remoteRepositories)
  // .withLocalRepository(request.getLocalRepositoryPath())
  // .withUserSettings(request.getUserSettingsFile())
  // .withGlobalSettings(request.getGlobalSettingsFile())
  // .build();
  // }
  // return aetherMavenClient;
  // }


  // protected List<ArtifactCoordinates> toArtifactCoordinates(List<Dependency> dependencies) {
  // return dependencies.stream().map(ArtifactUtils::toArtifactCoordinates).collect(Collectors.toList());
  // }

  // protected ProjectInformation getAndSetProjectInformation() {
  // ProjectInformation.Builder builder = new ProjectInformation.Builder();
  // if (projectInformation == null) {
  // boolean isDeployment = session.getGoals().stream().anyMatch(goal -> StringUtils.equals(goal, "deploy"));
  // builder.withGroupId(project.getGroupId())
  // .withArtifactId(project.getArtifactId())
  // .withVersion(project.getVersion())
  // .withPackaging(project.getPackaging())
  // .withClassifier(getClassifier())
  // .withProjectBaseFolder(Paths.get(projectBaseFolder.toURI()))
  // .withBuildDirectory(Paths.get(project.getBuild().getDirectory()))
  // .setTestProject(testJar)
  // .withDependencyProject(new DependencyProject(project))
  // .isDeployment(isDeployment);
  //
  // if (isDeployment) {
  // DistributionManagement management = project.getDistributionManagement();
  // DeploymentRepository repository = management != null ? management.getRepository() : null;
  // Settings settings = session.getSettings();
  // Optional<ExchangeRepositoryMetadata> metadata = getExchangeRepositoryMetadata(repository, settings);
  // if (metadata.isPresent()) {
  // builder.withExchangeRepositoryMetadata(metadata.get());
  // } else {
  // builder.withDeployments(Arrays.asList(agentDeployment, standaloneDeployment, armDeployment, cloudHubDeployment,
  // clusterDeployment));
  // }
  // }
  // projectInformation = builder.build();
  // }
  // return projectInformation;
  // }

  /**
   * This method avoids running a MoJo more than once.
   *
   * @return true if the MoJo run has already happened before
   */
  protected boolean hasExecutedBefore() {
    Map<String, String> pluginContext = getPluginContext();
    if (pluginContext.containsKey(getPreviousRunPlaceholder())) {
      return true;
    }
    getPluginContext().put(getPreviousRunPlaceholder(), getPreviousRunPlaceholder());
    return false;
  }


  // private Optional<ExchangeRepositoryMetadata> getExchangeRepositoryMetadata(DeploymentRepository repository, Settings
  // settings) {
  // ExchangeRepositoryMetadata metadata = null;
  // if (repository != null && ExchangeRepositoryMetadata.isExchangeRepo(repository.getUrl())) {
  // Server server = settings.getServer(repository.getId());
  // Credentials credentials = new Credentials(server.getUsername(), server.getPassword());
  // metadata = new ExchangeRepositoryMetadata(credentials, repository.getUrl());
  // }
  // return Optional.ofNullable(metadata);
  // }

  // mule-packager

  // protected PackagingType getPackagingType() {
  // return PackagingType.fromString(project.getPackaging());
  // }

  // mule-packager

  // public String getClassifier() {
  // return getPackagingType().resolveClassifier(classifier, lightweightPackage, testJar);
  // }
}

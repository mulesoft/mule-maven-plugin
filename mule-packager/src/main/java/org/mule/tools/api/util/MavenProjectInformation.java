/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.validation.exchange.ExchangeRepositoryMetadata;
import org.mule.tools.client.authentication.model.Credentials;
import org.mule.tools.model.Deployment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.mule.tools.model.anypoint.AnypointDeployment;

public class MavenProjectInformation implements ProjectInformation {

  private ProjectInformation projectInformation;

  private static final MavenProjectInformation mavenProjectInformation = new MavenProjectInformation();

  private MavenProjectInformation() {

  }

  public static MavenProjectInformation getProjectInformation(MavenSession session, MavenProject project, File projectBaseFolder,
                                                              boolean testJar, List<Deployment> deployments, String classifier) {
    DefaultProjectInformation.Builder builder = new DefaultProjectInformation.Builder();
    boolean isDeployment = isDeploymentGoal(session);
    builder.withGroupId(project.getGroupId())
        .withArtifactId(project.getArtifactId())
        .withVersion(project.getVersion())
        .withPackaging(project.getPackaging())
        .withClassifier(classifier)
        .withProjectBaseFolder(Paths.get(projectBaseFolder.toURI()))
        .withBuildDirectory(Paths.get(project.getBuild().getDirectory()))
        .setTestProject(testJar)
        .withDependencyProject(new DependencyProject(project))
        .isDeployment(isDeployment)
        .withResolvedPom(new ResolvedPom(project.getModel()));

    if (isDeployment) {
      DistributionManagement management = project.getDistributionManagement();
      DeploymentRepository repository = management != null ? management.getRepository() : null;
      Settings settings = session.getSettings();
      Optional<ExchangeRepositoryMetadata> metadata = getExchangeRepositoryMetadata(repository, settings);
      if (metadata.isPresent()) {
        if (deployments != null && isPlatformDeployment(deployments)) {
          builder.withDeployments(deployments);
        }
      } else {
        builder.withDeployments(deployments);
      }
    }
    mavenProjectInformation.projectInformation = builder.build();
    return mavenProjectInformation;
  }

  private static boolean isDeploymentGoal(MavenSession session) {
    return session.getGoals().stream().anyMatch(goal -> containsIgnoreCase(goal, "deploy"))
        || session.getSystemProperties().getProperty("muleDeploy") != null;
  }

  private static boolean isPlatformDeployment(List<Deployment> deployments) {
    return deployments.stream().anyMatch(d -> d instanceof AnypointDeployment);
  }

  @Override
  public String getGroupId() {
    return projectInformation.getGroupId();
  }

  @Override
  public String getArtifactId() {
    return projectInformation.getArtifactId();
  }

  @Override
  public String getVersion() {
    return projectInformation.getVersion();
  }

  @Override
  public String getClassifier() {
    return projectInformation.getClassifier();
  }

  @Override
  public String getPackaging() {
    return projectInformation.getPackaging();
  }

  @Override
  public Path getProjectBaseFolder() {
    return projectInformation.getProjectBaseFolder();
  }

  @Override
  public Path getBuildDirectory() {
    return projectInformation.getBuildDirectory();
  }

  @Override
  public boolean isTestProject() {
    return projectInformation.isTestProject();
  }

  @Override
  public Project getProject() {
    return projectInformation.getProject();
  }

  @Override
  public Optional<ExchangeRepositoryMetadata> getExchangeRepositoryMetadata() {
    return projectInformation.getExchangeRepositoryMetadata();
  }

  @Override
  public boolean isDeployment() {
    return projectInformation.isDeployment();
  }

  @Override
  public List<Deployment> getDeployments() {
    return projectInformation.getDeployments();
  }

  @Override
  public Pom getEffectivePom() {
    return projectInformation.getEffectivePom();
  }

  private static Optional<ExchangeRepositoryMetadata> getExchangeRepositoryMetadata(DeploymentRepository repository,
                                                                                    Settings settings) {
    ExchangeRepositoryMetadata metadata = null;
    if (repository != null && ExchangeRepositoryMetadata.isExchangeRepo(repository.getUrl())) {
      Server server = settings.getServer(repository.getId());
      if (server != null) {
        Credentials credentials = new Credentials(server.getUsername(), server.getPassword());
        metadata = new ExchangeRepositoryMetadata(credentials, repository.getUrl());
      }

    }
    return Optional.ofNullable(metadata);
  }
}

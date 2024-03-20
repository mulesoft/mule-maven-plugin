/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.util.ResolvedPom;
import org.mule.tools.api.util.SourcesProcessor;
import org.mule.tools.api.validation.exchange.ExchangeRepositoryMetadata;
import org.mule.tools.model.Deployment;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DomainBundleProjectValidatorTest {

  @TempDir
  public Path buildFolder;

  @Test
  public void domainBundleProjectValidatorTest() throws ValidationException {
    List<Deployment> deployments = new ArrayList<Deployment>();
    Project project = mock(Project.class);
    DefaultProjectInformation.Builder builder = new DefaultProjectInformation.Builder();
    ResolvedPom pom = mock(ResolvedPom.class);
    DefaultProjectInformation projectInformation = builder.withGroupId("groupId").withArtifactId("artifactId")
        .withVersion("1.0.0").withPackaging("packaging").withClassifier("classifier").withProjectBaseFolder(buildFolder)
        .withDependencyProject(project).withDeployments(deployments).withBuildDirectory(buildFolder).setTestProject(false)
        .isDeployment(false).withResolvedPom(pom).build();
    MavenClient mavenClient = null;
    DomainBundleProjectValidator validator = new DomainBundleProjectValidator(projectInformation, mavenClient);
    assertThatThrownBy(() -> validator.validateDomain(new HashSet<ArtifactCoordinates>()))
        .hasMessageContaining("A mule domain bundle must contain exactly one mule domain");
  }

  @Test
  public void domainBundleProjectValidatorValidateSingleApplicationTest() throws ValidationException {
    List<Deployment> deployments = new ArrayList<Deployment>();
    Project project = mock(Project.class);
    DefaultProjectInformation.Builder builder = new DefaultProjectInformation.Builder();
    ResolvedPom pom = mock(ResolvedPom.class);
    DefaultProjectInformation projectInformation = builder.withGroupId("groupId").withArtifactId("artifactId")
        .withVersion("1.0.0").withPackaging("packaging").withClassifier("classifier").withProjectBaseFolder(buildFolder)
        .withDependencyProject(project).withDeployments(deployments).withBuildDirectory(buildFolder).setTestProject(false)
        .isDeployment(false).withResolvedPom(pom).build();
    MavenClient mavenClient = mock(MavenClient.class);
    when(mavenClient.resolveBundleDescriptorDependencies(anyBoolean(), anyBoolean(), any()))
        .thenReturn(new ArrayList<BundleDependency>());
    DomainBundleProjectValidator validator = new DomainBundleProjectValidator(projectInformation, mavenClient);
    assertThatThrownBy(() -> validator.validateApplication(null, new ArtifactCoordinates("groupId", "artifactId", "version")))
        .hasMessageContaining("Every application in the domain bundle must refer to the specified domain");
  }

  @Test
  public void domainBundleProjectValidatorAdditionalValidationTest() throws ValidationException {
    List<Deployment> deployments = new ArrayList<Deployment>();
    Project project = mock(Project.class);
    DefaultProjectInformation.Builder builder = new DefaultProjectInformation.Builder();
    ResolvedPom pom = mock(ResolvedPom.class);
    DefaultProjectInformation projectInformation = builder.withGroupId("groupId").withArtifactId("artifactId")
        .withVersion("1.0.0").withPackaging("packaging").withClassifier("classifier").withProjectBaseFolder(buildFolder)
        .withDependencyProject(project).withDeployments(deployments).withBuildDirectory(buildFolder).setTestProject(false)
        .isDeployment(false).withResolvedPom(pom).build();
    MavenClient mavenClient = null;
    DomainBundleProjectValidator validator = new DomainBundleProjectValidator(projectInformation, mavenClient);
    assertThatThrownBy(() -> validator.additionalValidation())
        .hasMessageContaining("A mule domain bundle must contain exactly one mule domain");
  }

  @Test
  public void domainBundleProjectValidatorValidateMultipleAppsTest() throws ValidationException {
    List<Deployment> deployments = new ArrayList<Deployment>();
    Project project = mock(Project.class);
    DefaultProjectInformation.Builder builder = new DefaultProjectInformation.Builder();
    ResolvedPom pom = mock(ResolvedPom.class);
    DefaultProjectInformation projectInformation = builder.withGroupId("groupId").withArtifactId("artifactId")
        .withVersion("1.0.0").withPackaging("packaging").withClassifier("classifier").withProjectBaseFolder(buildFolder)
        .withDependencyProject(project).withDeployments(deployments).withBuildDirectory(buildFolder).setTestProject(false)
        .isDeployment(false).withResolvedPom(pom).build();
    MavenClient mavenClient = null;
    DomainBundleProjectValidator validator = new DomainBundleProjectValidator(projectInformation, mavenClient);
    assertThatThrownBy(() -> validator.validateApplications(null, null))
        .hasMessageContaining("A domain bundle should contain at least one application");
  }



}

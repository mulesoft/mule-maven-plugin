/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.classloader.model.Artifact.MULE_DOMAIN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.google.common.collect.Lists;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.api.packager.builder.MulePackageBuilder;

class AbstractMuleMojoTest {

  protected static final String GROUP_ID = "fake.group.id";
  protected static final String ARTIFACT_ID = "artifact-id";
  protected static final String VERSION = "1.0.0";

  protected static final String PACKAGE_NAME = "packageName";
  protected static final String MULE_APPLICATION = "mule-application";
  protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  protected Log logMock;
  protected Build buildMock;
  protected File metaInfFolder;
  protected File destinationFile;
  protected MavenProject projectMock;
  protected MavenSession mavenSessionMock;

  protected File muleSourceFolderMock;
  protected MulePackageBuilder packageBuilderMock;

  @TempDir
  public Path projectBaseFolder;

  @TempDir
  public Path buildFolderFolder;

  @TempDir
  public Path expectedException;

  @BeforeEach
  void beforeTest() throws IOException {
    metaInfFolder = createFolder(buildFolderFolder.resolve(META_INF.value()));
    System.setOut(new PrintStream(outContent));

    logMock = mock(Log.class);
    buildMock = mock(Build.class);
    projectMock = mock(MavenProject.class);
    mavenSessionMock = mock(MavenSession.class);
    packageBuilderMock = mock(MulePackageBuilder.class);
    muleSourceFolderMock = mock(File.class);

    when(projectMock.getBuild()).thenReturn(buildMock);
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    when(mavenSessionMock.getGoals()).thenReturn(Lists.newArrayList());

    Properties systemProperties = new Properties();
    systemProperties.put("muleDeploy", "false");
    when(mavenSessionMock.getSystemProperties()).thenReturn(systemProperties);

    when(buildMock.getDirectory()).thenReturn(buildFolderFolder.toFile().getAbsolutePath());
  }

  /**
   * The goal of this method is to pupulate a {@link AbstractGenericMojo} and create the proper behavior so it does not fail when
   * calling {@link AbstractGenericMojo#getProjectInformation()}
   *
   * @param mojo       the mojo to populate
   * @param groupId    the group id of the project
   * @param artifactId the artifact id of the project
   * @param version    the verion of the project
   */
  protected void prepareMojoForProjectInformation(AbstractGenericMojo mojo, String groupId, String artifactId, String version) {
    when(projectMock.getGroupId()).thenReturn(groupId);
    when(projectMock.getArtifactId()).thenReturn(artifactId);
    when(projectMock.getVersion()).thenReturn(version);
    when(projectMock.getModel()).thenReturn(mock(Model.class));

    mojo.project = projectMock;
    mojo.session = mavenSessionMock;
    mojo.projectBaseFolder = projectBaseFolder.toFile();
  }

  protected File createFolder(Path file) throws IOException {
    return Files.createDirectories(file).toFile();
  }

  protected void setProject(MavenProject project, String packaging, Boolean withDomains) {
    Build build = mock(Build.class);
    reset(project);

    when(build.getDirectory()).thenReturn(projectBaseFolder.toFile().getAbsolutePath());
    when(project.getPackaging()).thenReturn(packaging);
    when(project.getBasedir()).thenReturn(projectBaseFolder.toFile());
    when(project.getBuild()).thenReturn(build);
    when(project.getModel()).thenReturn(mock(Model.class));
    when(project.getGroupId()).thenReturn(UUID.randomUUID().toString());
    when(project.getArtifactId()).thenReturn(UUID.randomUUID().toString());
    when(project.getVersion()).thenReturn(UUID.randomUUID().toString());

    if (withDomains == null) {
      when(project.getDependencies()).thenReturn(null);
    } else if (withDomains) {
      List<Dependency> dependencies = new ArrayList<>(3);
      dependencies.add(createDependency(null));
      dependencies.add(createDependency(MULE_APPLICATION));
      dependencies.add(createDependency(MULE_DOMAIN));
      when(project.getDependencies()).thenReturn(dependencies);
    } else {
      when(project.getDependencies()).thenReturn(Collections.emptyList());
    }

  }

  private Dependency createDependency(String classifier) {
    Dependency dependency = mock(Dependency.class);
    when(dependency.getClassifier()).thenReturn(classifier);
    return dependency;
  }
}

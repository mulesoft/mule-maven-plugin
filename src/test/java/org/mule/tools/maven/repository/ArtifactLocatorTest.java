/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;

public class ArtifactLocatorTest {

  private static final String DUMMY_PATH = "/dummy-path/dummyDummy";
  private static final String RESOLVED_POM_ARTIFACT_REPRESENTATION = "resolved-pom-artifact";
  private static final int NUMBER_ARTIFACTS = 10;
  private File dummyFile;
  private ArtifactLocator artifactLocator;
  private Log logMock;
  private MavenProject projectMock;
  private ProjectBuilder projectBuilderMock;
  private RepositorySystem repositorySystemMock;
  private ArtifactRepository localRepositoryMock;
  private ProjectBuildingRequest projectBuildingRequestMock;
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String SCOPE = "compile";
  private static final String TYPE = "zip";
  private static final String CLASSIFIER = "classifier";
  private TemporaryFolder temporaryFolder;


  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void before() {
    temporaryFolder = new TemporaryFolder();
    dummyFile = new File(temporaryFolder.getRoot(), "/");
    logMock = mock(Log.class);
    projectMock = mock(MavenProject.class);
    projectBuilderMock = mock(ProjectBuilder.class, RETURNS_DEEP_STUBS);
    repositorySystemMock = mock(RepositorySystem.class);
    localRepositoryMock = mock(ArtifactRepository.class);
    projectBuildingRequestMock = mock(ProjectBuildingRequest.class);
    artifactLocator = new ArtifactLocator(logMock, projectMock, projectBuilderMock,
                                          repositorySystemMock, localRepositoryMock,
                                          projectBuildingRequestMock);
  }

  @Test
  public void validatePomArtifactFileThatWhenItHasNoReferenceToFileTest() throws MojoExecutionException {
    expectedException.expect(MojoExecutionException.class);
    expectedException
        .expectMessage("There was a problem trying to resolve the artifact's file location for ["
            + RESOLVED_POM_ARTIFACT_REPRESENTATION + "], file was null");

    Artifact resolvedPomArtifactMock = mock(Artifact.class);
    when(resolvedPomArtifactMock.getFile()).thenReturn(null);
    when(resolvedPomArtifactMock.toString()).thenReturn(RESOLVED_POM_ARTIFACT_REPRESENTATION);
    artifactLocator.validatePomArtifactFile(resolvedPomArtifactMock);

    verify(resolvedPomArtifactMock, times(1)).getFile();
    verify(resolvedPomArtifactMock, times(1)).toString();
  }

  @Test
  public void validatePomArtifactFileThatWhenFileDoesNotExistTest() throws MojoExecutionException {
    expectedException.expect(MojoExecutionException.class);
    expectedException
        .expectMessage("There was a problem trying to resolve the artifact's file location for ["
            + RESOLVED_POM_ARTIFACT_REPRESENTATION + "], file [" + DUMMY_PATH + "] doesn't exist");

    Artifact resolvedPomArtifactMock = mock(Artifact.class);
    File fileMock = mock(File.class);
    when(fileMock.exists()).thenReturn(false);
    when(fileMock.getAbsolutePath()).thenReturn(DUMMY_PATH);
    when(resolvedPomArtifactMock.getFile()).thenReturn(fileMock);
    when(resolvedPomArtifactMock.toString()).thenReturn(RESOLVED_POM_ARTIFACT_REPRESENTATION);
    artifactLocator.validatePomArtifactFile(resolvedPomArtifactMock);

    verify(fileMock, times(1)).getAbsolutePath();
    verify(resolvedPomArtifactMock, times(3)).getFile();
    verify(resolvedPomArtifactMock, times(1)).toString();
  }

  @Test
  public void getResolvedArtifactUsingLocalRepositoryTest() throws MojoExecutionException {
    ArtifactLocator artifactLocatorSpy = spy(artifactLocator);
    Artifact pomArtifactMock = mock(Artifact.class);
    Artifact resolvedPomArtifactMock = mock(Artifact.class);

    when(localRepositoryMock.find(pomArtifactMock)).thenReturn(resolvedPomArtifactMock);
    doNothing().when(artifactLocatorSpy).validatePomArtifactFile(resolvedPomArtifactMock);

    Artifact actualResolvedPomArtifact = artifactLocatorSpy.getResolvedArtifactUsingLocalRepository(pomArtifactMock);

    verify(localRepositoryMock, times(1)).find(pomArtifactMock);
    verify(artifactLocatorSpy, times(1)).validatePomArtifactFile(resolvedPomArtifactMock);

    assertThat("The resolved pom artifact was not the expected", actualResolvedPomArtifact, equalTo(resolvedPomArtifactMock));
  }

  @Test
  public void addParentDependencyPomArtifactsNoParentsTest() throws MojoExecutionException {
    ArtifactLocator artifactLocatorSpy = spy(artifactLocator);

    MavenProject mavenProjectMock1 = buildMavenProjectMock(1, null, null, null);

    Set<Artifact> artifacts = new HashSet<>();
    artifactLocatorSpy.addParentDependencyPomArtifacts(mavenProjectMock1, artifacts);

    verify(mavenProjectMock1, times(1)).hasParent();
    assertThat("Set of artifacts should be empty", artifacts.isEmpty());

  }

  @Test
  public void addParentDependencyPomArtifactsWithParentsTest() throws MojoExecutionException {

    ArtifactLocator artifactLocatorSpy = spy(artifactLocator);

    Artifact artifact1 = buildArtifact(1);
    MavenProject mavenProjectMock1 = buildMavenProjectMock(1, null, artifact1, null);
    doReturn(artifact1).when(artifactLocatorSpy).getResolvedArtifactUsingLocalRepository(artifact1);

    Artifact artifact2 = buildArtifact(2);
    MavenProject mavenProjectMock2 = buildMavenProjectMock(2, mavenProjectMock1, artifact2, null);
    doReturn(artifact2).when(artifactLocatorSpy).getResolvedArtifactUsingLocalRepository(artifact2);

    Artifact artifact3 = buildArtifact(3);;
    MavenProject mavenProjectMock3 = buildMavenProjectMock(3, mavenProjectMock2, artifact3, null);
    doReturn(artifact3).when(artifactLocatorSpy).getResolvedArtifactUsingLocalRepository(artifact3);

    Set<Artifact> artifacts = new HashSet<>();
    artifactLocatorSpy.addParentDependencyPomArtifacts(mavenProjectMock3, artifacts);

    verify(artifactLocatorSpy, times(1)).getResolvedArtifactUsingLocalRepository(artifact1);

    verify(mavenProjectMock1, times(1)).hasParent();
    verify(mavenProjectMock1, times(1)).getArtifact();


    verify(mavenProjectMock2, times(1)).hasParent();
    verify(mavenProjectMock2, times(1)).getArtifact();
    verify(mavenProjectMock2, times(1)).getParent();

    verify(mavenProjectMock3, times(1)).hasParent();
    verify(mavenProjectMock3, times(1)).getParent();

    assertThat("The set of artifacts is not the expected", artifacts, containsInAnyOrder(artifact1, artifact2));
  }

  @Test
  public void getArtifactsTest() throws MojoExecutionException {
    ArtifactLocator artifactLocatorSpy = spy(artifactLocator);
    doNothing().when(artifactLocatorSpy).addThirdPartyParentPomArtifacts(anySet(), any(Artifact.class));
    doNothing().when(artifactLocatorSpy).addParentPomArtifacts(anySet());
    Set<Artifact> artifacts = buildSetOfArtifacts();
    when(projectMock.getArtifacts()).thenReturn(artifacts);

    artifactLocatorSpy.getArtifacts();

    verify(projectMock, times(1)).getArtifacts();
    for (Artifact artifact : artifacts) {
      verify(artifactLocatorSpy, times(1)).addThirdPartyParentPomArtifacts(artifacts, artifact);
    }
    verify(artifactLocatorSpy, times(1)).addParentPomArtifacts(artifacts);
  }

  @Test
  public void addThirdPartyParentPomArtifactsTest() throws MojoExecutionException {
    ArtifactLocator artifactLocatorSpy = spy(artifactLocator);
    Artifact artifact = buildArtifact(10);
    Set<Artifact> artifacts = buildSetOfArtifacts();
    MavenProject mavenProjectMock = mock(MavenProject.class);
    doReturn(mavenProjectMock).when(artifactLocatorSpy).buildProjectFromArtifact(artifact);
    doNothing().when(artifactLocatorSpy).addParentDependencyPomArtifacts(mavenProjectMock, artifacts);
    when(repositorySystemMock.createProjectArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()))
        .thenReturn(artifact);
    doReturn(artifact).when(artifactLocatorSpy).getResolvedArtifactUsingLocalRepository(artifact);

    artifactLocatorSpy.addThirdPartyParentPomArtifacts(artifacts, artifact);

    verify(artifactLocatorSpy, times(1)).addThirdPartyParentPomArtifacts(artifacts, artifact);
    verify(artifactLocatorSpy, times(1)).addParentDependencyPomArtifacts(mavenProjectMock, artifacts);
    verify(repositorySystemMock, times(1)).createProjectArtifact(GROUP_ID + ".10", ARTIFACT_ID + "-10", VERSION);
    verify(artifactLocatorSpy, times(1)).getResolvedArtifactUsingLocalRepository(artifact);

    Set<Artifact> expectedArtifacts = buildSetOfArtifacts();
    assertThat("The artifact was not added", expectedArtifacts.add(artifact));
    assertThat("Artifacts are not the expected", artifacts, equalTo(expectedArtifacts));
  }

  @Test
  public void addParentPomArtifactsNoParentTest() throws MojoExecutionException {
    Set<Artifact> artifacts = new HashSet<>();

    artifactLocator.addParentPomArtifacts(artifacts);

    verify(projectMock, times(1)).hasParent();
    assertThat("Set of artifacts should be empty", artifacts.isEmpty());
  }

  @Test
  public void addParentPomArtifactsTest() throws MojoExecutionException {
    Artifact artifact1 = buildArtifact(1);
    MavenProject mavenProjectMock1 = buildMavenProjectMock(1, null, artifact1, dummyFile);

    Artifact artifact2 = buildArtifact(2);
    MavenProject mavenProjectMock2 = buildMavenProjectMock(2, mavenProjectMock1, artifact2, dummyFile);

    Artifact artifact3 = buildArtifact(3);
    when(projectMock.getArtifact()).thenReturn(artifact3);
    when(projectMock.hasParent()).thenReturn(true);
    when(projectMock.getParent()).thenReturn(mavenProjectMock2);
    when(projectMock.getFile()).thenReturn(dummyFile);

    Set<Artifact> artifacts = new HashSet<>();

    artifactLocator.addParentPomArtifacts(artifacts);

    verify(mavenProjectMock1, times(1)).hasParent();
    verify(mavenProjectMock1, times(1)).getArtifact();
    verify(mavenProjectMock1, times(0)).getParent();
    verify(mavenProjectMock1, times(2)).getFile();


    verify(mavenProjectMock2, times(1)).hasParent();
    verify(mavenProjectMock2, times(1)).getArtifact();
    verify(mavenProjectMock2, times(1)).getParent();
    verify(mavenProjectMock2, times(2)).getFile();

    verify(projectMock, times(1)).hasParent();
    verify(projectMock, times(0)).getArtifact();
    verify(projectMock, times(1)).getParent();
    verify(projectMock, times(0)).getFile();

    assertThat("Artifact set is not the expected", artifacts, containsInAnyOrder(artifact1, artifact2));
  }

  @Test
  public void addParentPomArtifactsWhenArtifactIsThereTest() throws MojoExecutionException {
    Artifact artifact1 = buildArtifact(1);
    MavenProject mavenProjectMock1 = buildMavenProjectMock(1, null, artifact1, dummyFile);

    Artifact artifact2 = buildArtifact(2);
    MavenProject mavenProjectMock2 = buildMavenProjectMock(2, mavenProjectMock1, artifact2, dummyFile);

    Artifact artifact3 = buildArtifact(3);
    when(projectMock.getArtifact()).thenReturn(artifact3);
    when(projectMock.hasParent()).thenReturn(true);
    when(projectMock.getParent()).thenReturn(mavenProjectMock2);
    when(projectMock.getFile()).thenReturn(dummyFile);

    Set<Artifact> artifacts = new HashSet<>();
    artifacts.add(artifact1);

    artifactLocator.addParentPomArtifacts(artifacts);

    verify(mavenProjectMock1, times(0)).hasParent();
    verify(mavenProjectMock1, times(1)).getArtifact();
    verify(mavenProjectMock1, times(0)).getParent();
    verify(mavenProjectMock1, times(2)).getFile();

    verify(mavenProjectMock2, times(1)).hasParent();
    verify(mavenProjectMock2, times(1)).getArtifact();
    verify(mavenProjectMock2, times(1)).getParent();
    verify(mavenProjectMock2, times(2)).getFile();

    verify(projectMock, times(1)).hasParent();
    verify(projectMock, times(0)).getArtifact();
    verify(projectMock, times(1)).getParent();
    verify(projectMock, times(0)).getFile();

    assertThat("Artifact set is not the expected", artifacts, containsInAnyOrder(artifact1, artifact2));
  }

  @Test
  public void addParentPomArtifactsNoFileTest() throws MojoExecutionException {
    ArtifactLocator artifactLocatorSpy = spy(artifactLocator);
    doAnswer((InvocationOnMock invocation) -> {
      Set<Artifact> artifacts = (Set<Artifact>) invocation.getArguments()[0];
      Artifact artifact = (Artifact) invocation.getArguments()[1];
      artifacts.add(artifact);
      return null;
    }).when(artifactLocatorSpy).addThirdPartyParentPomArtifacts(anySet(), any(Artifact.class));

    Artifact artifact1 = buildArtifact(1);
    MavenProject mavenProjectMock1 = buildMavenProjectMock(1, null, artifact1, null);

    Artifact artifact2 = buildArtifact(2);
    when(projectMock.getArtifact()).thenReturn(artifact2);
    when(projectMock.hasParent()).thenReturn(true);
    when(projectMock.getParent()).thenReturn(mavenProjectMock1);
    when(projectMock.getFile()).thenReturn(dummyFile);

    Set<Artifact> artifacts = new HashSet<>();
    artifactLocatorSpy.addParentPomArtifacts(artifacts);

    verify(artifactLocatorSpy, times(1)).addThirdPartyParentPomArtifacts(artifacts, artifact1);

    verify(mavenProjectMock1, times(1)).hasParent();
    verify(mavenProjectMock1, times(1)).getArtifact();
    verify(mavenProjectMock1, times(0)).getParent();
    verify(mavenProjectMock1, times(1)).getFile();

    verify(projectMock, times(1)).hasParent();
    verify(projectMock, times(0)).getArtifact();
    verify(projectMock, times(1)).getParent();
    verify(projectMock, times(0)).getFile();

    assertThat("Missing artifact was not added to resolved set", artifacts, containsInAnyOrder(artifact1));
  }

  @Test
  public void buildProjectFromArtifactTest() throws ProjectBuildingException, MojoExecutionException {
    Artifact artifact = buildArtifact(0);
    Artifact projectArtifactMock = mock(Artifact.class);
    when(repositorySystemMock.createProjectArtifact(GROUP_ID + ".0", ARTIFACT_ID + "-0", VERSION))
        .thenReturn(projectArtifactMock);
    when(projectBuilderMock.build(projectArtifactMock, projectBuildingRequestMock).getProject()).thenReturn(projectMock);

    MavenProject actualMavenProject = artifactLocator.buildProjectFromArtifact(artifact);

    assertThat("The project that was built is different from the expected", actualMavenProject, equalTo(projectMock));
  }

  private Set<Artifact> buildSetOfArtifacts() {
    HashSet<Artifact> artifacts = new HashSet<>();
    for (int i = 0; i < NUMBER_ARTIFACTS; ++i) {
      artifacts.add(buildArtifact(i));
    }
    return artifacts;
  }

  private MavenProject buildMavenProjectMock(int i, MavenProject parent, Artifact artifact, File file) {
    MavenProject mavenProjectMock = mock(MavenProject.class);
    when(mavenProjectMock.getArtifact()).thenReturn(artifact);
    when(mavenProjectMock.getFile()).thenReturn(file);
    when(mavenProjectMock.getParent()).thenReturn(parent);
    when(mavenProjectMock.hasParent()).thenReturn(parent != null);
    return mavenProjectMock;
  }

  private Artifact buildArtifact(int i) {
    ArtifactHandler artifactHandler = new DefaultArtifactHandler(TYPE);
    return new DefaultArtifact(GROUP_ID + "." + i, ARTIFACT_ID + "-" + i, VERSION, SCOPE, TYPE,
                               CLASSIFIER, artifactHandler);
  }
}

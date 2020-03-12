/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package util;

import static java.nio.file.Paths.get;
import static java.util.Optional.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import org.mule.maven.client.api.MavenReactorResolver;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.tools.api.util.MavenComponents;
import org.mule.tools.api.util.SourcesProcessor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SourcesProcessorTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private SourcesProcessor sourcesProcessor;

  private MavenProject project;

  @Before
  public void setUp() {

    MavenSession session = mock(MavenSession.class);
    this.project = buildMavenProjectMock();

    when(session.getRequest()).thenReturn(mock(MavenExecutionRequest.class));
    when(session.getProjectBuildingRequest()).thenReturn(mock(ProjectBuildingRequest.class));
    when(session.getCurrentProject()).thenReturn(project);
    when(session.getGoals()).thenReturn(Lists.newArrayList());

    Properties systemProperties = new Properties();
    systemProperties.put("muleDeploy", "false");
    when(session.getSystemProperties()).thenReturn(systemProperties);

    MavenComponents mavenComponents =
        new MavenComponents().withLog(mock(Log.class))
            .withProject(project)
            .withOutputDirectory(new File(temporaryFolder.getRoot(), "target"))
            .withSession(session)
            .withSharedLibraries(new Vector<>())
            .withProjectBuilder(mock(ProjectBuilder.class))
            .withRepositorySystem(mock(RepositorySystem.class))
            .withLocalRepository(mock(ArtifactRepository.class))
            .withRemoteArtifactRepositories(new Vector<>())
            .withClassifier("")
            .withAdditionalPluginDependencies(new Vector<>())
            .withProjectBaseFolder(temporaryFolder.getRoot());

    this.sourcesProcessor = new SourcesProcessor(mavenComponents);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullMavenComponents() {
    new SourcesProcessor(null);
  }

  @Test
  public void lightweightTestLocalRepository() throws Exception {
    sourcesProcessor
        .process(true, true, true, true, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "com", "mulesoft", "munit")
        .toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());
  }

  @Test
  public void lightweightLocalRepository() throws Exception {
    sourcesProcessor
        .process(true, true, true, false, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertFalse(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "com", "mulesoft",
                    "munit").toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());
  }

  @Test
  public void lightweightTest() throws Exception {
    sourcesProcessor
        .process(true, true, false, true, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertFalse(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertFalse(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());
  }

  @Test
  public void lightweight() throws Exception {
    sourcesProcessor
        .process(true, true, false, false, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertFalse(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertFalse(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());
  }

  @Test
  public void heavyweightLocalRepositoryTest() throws Exception {
    sourcesProcessor
        .process(true, false, true, true, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "com", "mulesoft", "munit")
        .toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());
  }

  @Test
  public void heavyweightLocalRepository() throws Exception {
    sourcesProcessor
        .process(true, false, true, false, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertFalse(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "com", "mulesoft",
                    "munit").toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());
  }

  @Test
  public void heavyweightTest() throws Exception {
    sourcesProcessor
        .process(true, false, false, true, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());
  }

  @Test
  public void heavyweight() throws Exception {
    sourcesProcessor
        .process(true, false, false, false, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());
  }

  @Test
  public void prettyPrinting() throws Exception {
    sourcesProcessor
        .process(false, false, true, true, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 empty());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "com", "mulesoft", "munit")
        .toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());
  }

  @Test
  public void mavenReactorResolverCalled() throws Exception {

    TestMavenReactorResolver reactorResolver = new TestMavenReactorResolver();

    when(project.getFile())
        .thenReturn(get(temporaryFolder.getRoot().getAbsolutePath(), "pom-with-dependency-to-resolve", "pom.xml").toFile());

    sourcesProcessor
        .process(true, false, true, true, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 Optional.of(reactorResolver));

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "com", "mulesoft", "munit")
        .toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());

    assertTrue(reactorResolver.getFindArtifactCalled());
    assertFalse(reactorResolver.getFindVersionsCalled());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "repository", "commons-io", "commons-io", "0.1",
                   "commons-io-0.1.jar")
                       .toFile().exists());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "repository", "com", "example", "dependency-test-app",
                   "1.0.0",
                   "dependency-test-app-1.0.0-mule-plugin.jar")
                       .toFile().exists());
  }

  @Test
  public void mavenReactorResolverCalledWithSnapshotVersion() throws Exception {

    TestMavenReactorResolver reactorResolver = new TestMavenReactorResolver();

    when(project.getFile())
        .thenReturn(get(temporaryFolder.getRoot().getAbsolutePath(), "pom-with-snapshot-dependency-to-resolve", "pom.xml")
            .toFile());

    sourcesProcessor
        .process(true, false, true, true, new File(temporaryFolder.getRoot(), "target"),
                 temporaryFolder.getRoot().toPath().resolve("target").resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
                     .toFile(),
                 Optional.of(reactorResolver));

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact").toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "com", "mulesoft", "munit")
        .toFile().exists());
    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "META-INF", "mule-artifact", "classloader-model.json")
        .toFile().exists());

    assertTrue(reactorResolver.getFindArtifactCalled());
    assertTrue(reactorResolver.getFindVersionsCalled());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "repository", "commons-io", "commons-io", "0.1",
                   "commons-io-0.1.jar")
                       .toFile().exists());

    assertTrue(get(temporaryFolder.getRoot().getAbsolutePath(), "target", "repository", "com", "example", "dependency-test-app",
                   "1.0.0-SNAPSHOT",
                   "dependency-test-app-1.0.0-SNAPSHOT-mule-plugin.jar")
                       .toFile().exists());
  }

  public MavenProject buildMavenProjectMock() {

    Build buildMock = mock(Build.class);

    try {
      FileUtils.copyDirectory(get("src", "test", "resources", "test-app").toFile(), temporaryFolder.getRoot());
    } catch (IOException e) {
      e.printStackTrace();
    }

    when(buildMock.getDirectory())
        .thenReturn(get(temporaryFolder.getRoot().getAbsolutePath(), "target").toAbsolutePath().toString());
    when(buildMock.getOutputDirectory())
        .thenReturn(get(temporaryFolder.getRoot().getAbsolutePath(), "target").toAbsolutePath().toString());
    when(buildMock.getTestOutputDirectory()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());

    MavenProject mavenProjectMock = mock(MavenProject.class);
    when(mavenProjectMock.getBuild()).thenReturn(buildMock);
    when(mavenProjectMock.getGroupId()).thenReturn("com.mycompany");
    when(mavenProjectMock.getVersion()).thenReturn("1.0.0-SNAPSHOT");
    when(mavenProjectMock.getArtifactId()).thenReturn("test-app");
    when(mavenProjectMock.getFile()).thenReturn(new File(temporaryFolder.getRoot(), "pom.xml"));
    when(mavenProjectMock.getBasedir()).thenReturn(temporaryFolder.getRoot());
    when(mavenProjectMock.getPackaging()).thenReturn("mule-application");
    when(mavenProjectMock.getModel()).thenReturn(mock(Model.class));

    return mavenProjectMock;
  }

  private static class TestMavenReactorResolver implements MavenReactorResolver {

    private Boolean findArtifactCalled = false;
    private Boolean findVersionsCalled = false;
    private static final String ARTIFACTID = "dependency-test-app";

    @Override
    public File findArtifact(BundleDescriptor bundleDescriptor) {
      if (bundleDescriptor.getArtifactId().equals(ARTIFACTID)) {
        findArtifactCalled = true;
        return get("src", "test", "resources", "test-app", "MavenReactorResolver", "pom.xml").toFile();
      }
      return null;
    }

    @Override
    public List<String> findVersions(BundleDescriptor bundleDescriptor) {
      if (bundleDescriptor.getArtifactId().equals(ARTIFACTID)) {
        findVersionsCalled = true;
        return ImmutableList.of("1.0.0");
      }
      return null;
    }

    public Boolean getFindArtifactCalled() {
      return findArtifactCalled;
    }

    public Boolean getFindVersionsCalled() {
      return findVersionsCalled;
    }
  }

}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.classloader.Constants.MULE_MAVEN_PLUGIN_ARTIFACT_ID;
import static org.mule.tools.api.classloader.Constants.MULE_MAVEN_PLUGIN_GROUP_ID;
import static org.mule.tools.api.classloader.Constants.SHARED_LIBRARIES_FIELD;
import static org.mule.tools.api.classloader.Constants.SHARED_LIBRARY_FIELD;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifact;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ApplicationClassLoaderModelAssemblerTest {

  private static final String USER_REPOSITORY_LOCATION =
      "file://Users/muleuser/.m2/repository";
  private static final String SEPARATOR = "/";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String GROUP_ID_SEPARATOR = ".";
  private static final String MULE_DOMAIN_CLASSIFIER = "mule-domain";
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "jar";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private AetherMavenClient aetherMavenClientMock;

  @Parameterized.Parameter
  public Boolean legacyModel;

  @Parameterized.Parameters(name = "Running application class loader model assembler with legacyModel: {0}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {true},
        {false}
    });
  }

  @Test
  public void getClassLoaderModelTest() throws URISyntaxException {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1, EMPTY);
    BundleDependency dependency2 =
        buildBundleDependency(1, 2, EMPTY);
    BundleDependency dependency3 =
        buildBundleDependency(1, 3, MULE_DOMAIN_CLASSIFIER);
    appDependencies.add(dependency1);
    appDependencies.add(dependency2);
    appDependencies.add(dependency3);

    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();
    BundleDependency firstMulePlugin =
        buildBundleDependency(2, 3, MULE_PLUGIN_CLASSIFIER);
    BundleDependency secondMulePlugin = buildBundleDependency(2, 4,
                                                              MULE_PLUGIN_CLASSIFIER);
    appMulePluginDependencies.add(firstMulePlugin);
    appMulePluginDependencies.add(secondMulePlugin);

    aetherMavenClientMock = getAetherMavenClientMock(appDependencies, appMulePluginDependencies);

    List<BundleDependency> firstMulePluginDependencies = new ArrayList<>();
    BundleDependency dependency5 =
        buildBundleDependency(1, 5, EMPTY);
    BundleDependency dependency6 = buildBundleDependency(1, 6,
                                                         EMPTY);
    BundleDependency dependency7 = buildBundleDependency(1, 7, EMPTY);
    BundleDependency dependency8 = buildBundleDependency(1, 8, EMPTY);
    firstMulePluginDependencies.add(secondMulePlugin);
    firstMulePluginDependencies.add(dependency5);
    firstMulePluginDependencies.add(dependency8);

    setPluginDependencyinAetherMavenClientMock(firstMulePlugin, firstMulePluginDependencies);

    List<BundleDependency> secondMulePluginDependencies = new ArrayList<>();
    secondMulePluginDependencies.add(dependency6);
    secondMulePluginDependencies.add(dependency7);

    setPluginDependencyinAetherMavenClientMock(secondMulePlugin, secondMulePluginDependencies);

    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        getClassLoaderModelAssemblySpy(aetherMavenClientMock);

    ApplicationClassloaderModel applicationClassloaderModel =
        applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class), legacyModel);

    assertThat("Application dependencies are not the expected",
               applicationClassloaderModel.getClassLoaderModel().getDependencies(),
               containsInAnyOrder(toArtifact(firstMulePlugin), toArtifact(secondMulePlugin), toArtifact(dependency1),
                                  toArtifact(dependency2), toArtifact(dependency3)));
  }

  @Test
  public void getClassLoaderModelWithSharedDependencies() throws URISyntaxException {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1,
                                                         EMPTY);

    BundleDependency sharedLibrary = buildBundleDependency(2, 2,
                                                           EMPTY);
    appDependencies.add(dependency1);
    appDependencies.add(sharedLibrary);
    ApplicationClassloaderModel applicationClassloaderModel =
        testSharedDependencies(appDependencies, singletonList(sharedLibrary), Optional.empty());

    assertThat("Application contains both libraries",
               applicationClassloaderModel.getClassLoaderModel().getDependencies(),
               containsInAnyOrder(toArtifact(dependency1), toArtifact(sharedLibrary)));

    assertThat("Application shared library is marked as shared",
               applicationClassloaderModel.getClassLoaderModel().getDependencies().stream().anyMatch(artifact -> artifact
                   .getArtifactCoordinates().getArtifactId().equals(sharedLibrary.getDescriptor().getArtifactId()) &&
                   artifact.getArtifactCoordinates().getGroupId().equals(sharedLibrary.getDescriptor().getGroupId()) &&
                   artifact.isShared()),
               is(true));
  }

  @Test
  public void getClassLoaderModelWithSharedDependenciesWithTransitiveDependencies() throws URISyntaxException {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1,
                                                         EMPTY);

    BundleDependency sharedLibraryTransitiveDependencyLevel2 = buildBundleDependency(2, 2,
                                                                                     EMPTY, "1.0.0", emptyList()).build();
    BundleDependency sharedLibraryTransitiveDependencyLevel1 = buildBundleDependency(3, 3,
                                                                                     EMPTY, "1.0.0",
                                                                                     singletonList(sharedLibraryTransitiveDependencyLevel2))
                                                                                         .build();
    BundleDependency sharedLibrary = buildBundleDependency(4, 4,
                                                           EMPTY, "1.0.0", singletonList(sharedLibraryTransitiveDependencyLevel1))
                                                               .build();

    appDependencies.add(dependency1);
    appDependencies.add(sharedLibraryTransitiveDependencyLevel2);
    appDependencies.add(sharedLibraryTransitiveDependencyLevel1);
    appDependencies.add(sharedLibrary);

    ApplicationClassloaderModel applicationClassloaderModel =
        testSharedDependencies(appDependencies, singletonList(sharedLibrary), Optional.empty());

    assertThat("Application contains all libraries",
               applicationClassloaderModel.getClassLoaderModel().getDependencies(),
               containsInAnyOrder(toArtifact(dependency1), toArtifact(sharedLibrary),
                                  toArtifact(sharedLibraryTransitiveDependencyLevel1),
                                  toArtifact(sharedLibraryTransitiveDependencyLevel2)));

    assertThat("Application contains shared libraries with transitive dependencies",
               applicationClassloaderModel.getClassLoaderModel().getDependencies().stream()
                   .filter(Artifact::isShared).collect(toList()),
               containsInAnyOrder(toArtifact(sharedLibrary),
                                  toArtifact(sharedLibraryTransitiveDependencyLevel1),
                                  toArtifact(sharedLibraryTransitiveDependencyLevel2)));

    assertThat("Application shared library is marked as shared",
               applicationClassloaderModel.getClassLoaderModel().getDependencies().stream().anyMatch(artifact -> artifact
                   .getArtifactCoordinates().getArtifactId().equals(sharedLibrary.getDescriptor().getArtifactId()) &&
                   artifact.getArtifactCoordinates().getGroupId().equals(sharedLibrary.getDescriptor().getGroupId()) &&
                   artifact.isShared()),
               is(true));
  }

  private ApplicationClassloaderModel testSharedDependencies(List<BundleDependency> appDependencies,
                                                             List<BundleDependency> sharedLibraries,
                                                             Optional<Model> providedModel) {


    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();

    aetherMavenClientMock = getAetherMavenClientMock(appDependencies, appMulePluginDependencies);

    when(aetherMavenClientMock.resolveBundleDescriptorDependencies(eq(false), eq(false), any()))
        .thenReturn(appMulePluginDependencies);

    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        getClassLoaderModelAssemblySpy(aetherMavenClientMock);

    Model artifactPomModel = providedModel.orElse(createArtifactModel(sharedLibraries));

    doReturn(artifactPomModel).when(applicationClassLoaderModelAssemblerSpy).getPomFile(any());

    ApplicationClassloaderModel applicationClassloaderModel =
        applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class), legacyModel);

    return applicationClassloaderModel;
  }

  @Test
  public void noBuildInModelDoesNotFail() {
    testSharedDependencies(emptyList(), emptyList(), Optional.of(new Model()));
  }

  @Test
  public void noMavenPluginInModelDoesNotFail() {
    Model model = new Model();
    model.setBuild(new Build());
    testSharedDependencies(emptyList(), emptyList(), Optional.of(model));
  }

  @Test
  public void noConfigurationInMavenPluginDoesNotFail() {
    Model model = new Model();
    Build build = new Build();
    Plugin plugin = new Plugin();
    plugin.setGroupId(MULE_MAVEN_PLUGIN_GROUP_ID);
    plugin.setArtifactId(MULE_MAVEN_PLUGIN_ARTIFACT_ID);
    build.addPlugin(plugin);
    model.setBuild(build);
    testSharedDependencies(emptyList(), emptyList(), Optional.of(model));
  }

  private Model createArtifactModel(List<BundleDependency> sharedLibraries) {
    Model artifactPomModel = new Model();
    Plugin plugin = new Plugin();
    plugin.setArtifactId(MULE_MAVEN_PLUGIN_ARTIFACT_ID);
    plugin.setGroupId(MULE_MAVEN_PLUGIN_GROUP_ID);
    Xpp3Dom configuration = new Xpp3Dom("configuration");
    plugin.setConfiguration(configuration);
    Xpp3Dom sharedLibrariesDom = new Xpp3Dom(SHARED_LIBRARIES_FIELD);
    configuration.addChild(sharedLibrariesDom);
    sharedLibraries.stream()
        .forEach(sharedLibraryToAdd -> {
          Xpp3Dom sharedLibraryDom = new Xpp3Dom(SHARED_LIBRARY_FIELD);
          sharedLibrariesDom.addChild(sharedLibraryDom);
          Xpp3Dom groupIdDom = new Xpp3Dom("groupId");
          groupIdDom.setValue(sharedLibraryToAdd.getDescriptor().getGroupId());
          sharedLibraryDom.addChild(groupIdDom);
          Xpp3Dom artifactIdDom = new Xpp3Dom("artifactId");
          artifactIdDom.setValue(sharedLibraryToAdd.getDescriptor().getArtifactId());
          sharedLibraryDom.addChild(artifactIdDom);
        });

    artifactPomModel.setBuild(new Build());
    artifactPomModel.getBuild().addPlugin(plugin);
    return artifactPomModel;
  }

  @Test
  public void getClassLoaderModelWithOneDependencyThatIsNotMulePluginTest() throws URISyntaxException {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1,
                                                         EMPTY);
    appDependencies.add(dependency1);

    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();

    aetherMavenClientMock = getAetherMavenClientMock(appDependencies, appMulePluginDependencies);

    when(aetherMavenClientMock.resolveBundleDescriptorDependencies(eq(false), eq(false), any())).thenReturn(new ArrayList<>());

    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        getClassLoaderModelAssemblySpy(aetherMavenClientMock);

    ApplicationClassloaderModel applicationClassloaderModel =
        applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class), legacyModel);

    assertThat("Application dependencies are not the expected",
               applicationClassloaderModel.getClassLoaderModel().getDependencies(),
               containsInAnyOrder(toArtifact(dependency1)));

    if (legacyModel) {
      assertThat("The application should have no mule plugin dependencies",
                 applicationClassloaderModel.getNestedClassLoaderModels().size(),
                 equalTo(0));
    } else {
      assertThat("The application should have no mule plugin dependencies",
                 applicationClassloaderModel.getArtifacts().stream()
                     .filter(artifact -> artifact.getArtifactCoordinates().getClassifier().equals(MULE_PLUGIN_CLASSIFIER))
                     .collect(toList())
                     .size(),
                 equalTo(0));
    }
  }

  @Test
  public void getClassLoaderModelWithOneDependencyThatIsAMulePluginTest() throws URISyntaxException {
    List<BundleDependency> appDependencies = new ArrayList<>();

    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();
    BundleDependency firstMulePlugin =
        buildBundleDependency(2, 3, MULE_PLUGIN_CLASSIFIER);
    appMulePluginDependencies.add(firstMulePlugin);

    aetherMavenClientMock = getAetherMavenClientMock(appDependencies, appMulePluginDependencies);

    List<BundleDependency> firstMulePluginDependencies = new ArrayList<>();
    BundleDependency mulePluginTransitiveDependency1 =
        buildBundleDependency(1, 1, EMPTY);
    firstMulePluginDependencies.add(mulePluginTransitiveDependency1);

    setPluginDependencyinAetherMavenClientMock(firstMulePlugin, firstMulePluginDependencies);

    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        getClassLoaderModelAssemblySpy(aetherMavenClientMock);

    ApplicationClassloaderModel applicationClassloaderModel =
        applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class), legacyModel);

    assertThat("The class loader model should have one dependency",
               applicationClassloaderModel.getClassLoaderModel().getDependencies().size(),
               equalTo(1));

    assertThat("Mule plugins are not the expected", applicationClassloaderModel.getClassLoaderModel().getDependencies(),
               containsInAnyOrder(toArtifact(firstMulePlugin)));

    if (legacyModel) {
      assertThat("Mule plugin dependencies are not the expected",
                 applicationClassloaderModel.getNestedClassLoaderModels().values().iterator().next().getDependencies(),
                 containsInAnyOrder(toArtifact(mulePluginTransitiveDependency1)));
    } else {
      assertThat("Mule plugin dependencies are not the expected",
                 applicationClassloaderModel.getArtifacts().stream()
                     .filter(artifact -> artifact.getArtifactCoordinates().getClassifier().equals(MULE_PLUGIN_CLASSIFIER))
                     .findFirst().orElseThrow(() -> new AssertionError("Couldn't find mule plugin")).getDependencies(),
                 containsInAnyOrder(toArtifact(mulePluginTransitiveDependency1)));
    }
  }


  private BundleDependency buildBundleDependency(int groupIdSuffix, int artifactIdSuffix, String classifier)
      throws URISyntaxException {
    return buildBundleDependency(groupIdSuffix, artifactIdSuffix, classifier, VERSION, emptyList()).build();
  }

  private BundleDependency.Builder buildBundleDependency(int groupIdSuffix, int artifactIdSuffix, String classifier,
                                                         String version, List<BundleDependency> transitiveDependencies)
      throws URISyntaxException {
    BundleDescriptor bundleDescriptor = buildBundleDescriptor(groupIdSuffix, artifactIdSuffix, classifier, version);
    URI bundleUri = buildBundleURI(bundleDescriptor);
    BundleDependency.Builder builder = new BundleDependency.Builder().setDescriptor(bundleDescriptor).setBundleUri(bundleUri);
    transitiveDependencies.stream().forEach(transitiveDependency -> builder.addTransitiveDependency(transitiveDependency));
    return builder;
  }

  private URI buildBundleURI(BundleDescriptor bundleDescriptor) throws URISyntaxException {
    return new URI(USER_REPOSITORY_LOCATION + SEPARATOR + bundleDescriptor.getGroupId().replace(GROUP_ID_SEPARATOR, SEPARATOR) +
        bundleDescriptor.getArtifactId() + SEPARATOR + bundleDescriptor.getBaseVersion());

  }

  private BundleDescriptor buildBundleDescriptor(int groupIdSuffix, int artifactIdSuffix, String classifier, String version) {
    return new BundleDescriptor.Builder().setGroupId(GROUP_ID + groupIdSuffix).setArtifactId(ARTIFACT_ID + artifactIdSuffix)
        .setVersion(version).setBaseVersion(version).setType(TYPE).setClassifier(classifier).build();
  }

  private ApplicationClassLoaderModelAssembler getClassLoaderModelAssemblySpy(AetherMavenClient aetherMavenClientMock) {
    try {
      ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
          spy(new ApplicationClassLoaderModelAssembler(aetherMavenClientMock, temporaryFolder.newFolder()));
      ArtifactCoordinates projectArtifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
      doReturn(new Model()).when(applicationClassLoaderModelAssemblerSpy).getPomFile(any());
      doReturn(projectArtifactCoordinates).when(applicationClassLoaderModelAssemblerSpy).getApplicationArtifactCoordinates(any());
      return applicationClassLoaderModelAssemblerSpy;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setPluginDependencyinAetherMavenClientMock(BundleDependency mulePlugin,
                                                          List<BundleDependency> mulePluginDependencies) {
    when(aetherMavenClientMock.resolveBundleDescriptorDependencies(eq(false), eq(false),
                                                                   eq(mulePlugin.getDescriptor())))
                                                                       .thenReturn(mulePluginDependencies);
  }

  private AetherMavenClient getAetherMavenClientMock(List<BundleDependency> appDependencies,
                                                     List<BundleDependency> appMulePluginDependencies) {
    AetherMavenClient aetherMavenClientMock = mock(AetherMavenClient.class);
    appDependencies.addAll(appMulePluginDependencies);
    when(aetherMavenClientMock.resolveArtifactDependencies(any(File.class), anyBoolean(),
                                                           anyBoolean(), any(Optional.class),
                                                           any(Optional.class), any(Optional.class)))
                                                               .thenReturn(appDependencies);
    when(aetherMavenClientMock.getEffectiveModel(any(), any()))
        .thenReturn(new Model());

    return aetherMavenClientMock;
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.muleclassloader.model;

import com.google.common.collect.Lists;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.internal.MuleMavenClient;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;
import org.mule.maven.pom.parser.internal.model.MavenPomModelWrapper;
import org.mule.tools.api.muleclassloader.model.resolver.AdditionalPluginDependenciesResolver;
import org.mule.tools.api.muleclassloader.model.resolver.ApplicationDependencyResolver;
import org.mule.tools.api.muleclassloader.model.resolver.MulePluginClassloaderModelResolver;
import org.mule.tools.api.util.FileJarExplorer;
import org.mule.tools.api.util.JarExplorer;
import org.mule.tools.api.util.JarInfo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.nio.file.Paths.get;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
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
import static org.mule.tools.api.muleclassloader.model.ApplicationClassLoaderModelAssembler.CLASSES;
import static org.mule.tools.api.muleclassloader.model.util.ArtifactUtils.toArtifact;
import static org.mule.tools.api.muleclassloader.model.util.ZipUtils.compress;
import static org.mule.tools.deployment.AbstractDeployerFactory.MULE_APPLICATION_CLASSIFIER;

class ApplicationClassLoaderModelAssemblerTest {

  private static final String SEPARATOR = "/";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String GROUP_ID_SEPARATOR = ".";
  private static final String MULE_DOMAIN_CLASSIFIER = "mule-domain";
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "jar";

  @TempDir
  public Path temporaryFolder;

  private MavenClient mavenClient;

  @Test
  void getClassLoaderModelTest() throws IOException {
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

    BundleDependency dependency5 = buildBundleDependency(1, 5, EMPTY);
    BundleDependency dependency6 = buildBundleDependency(1, 6, EMPTY);
    BundleDependency dependency7 = buildBundleDependency(1, 7, EMPTY);
    BundleDependency dependency8 = buildBundleDependency(1, 8, EMPTY);

    List<BundleDependency> secondMulePluginDependencies = new ArrayList<>();
    secondMulePluginDependencies.add(dependency6);
    secondMulePluginDependencies.add(dependency7);

    BundleDependency secondMulePlugin =
        buildBundleDependency(2, 4, MULE_PLUGIN_CLASSIFIER, VERSION, secondMulePluginDependencies).build();


    List<BundleDependency> firstMulePluginDependencies = new ArrayList<>();
    firstMulePluginDependencies.add(secondMulePlugin);
    firstMulePluginDependencies.add(dependency5);
    firstMulePluginDependencies.add(dependency8);

    BundleDependency firstMulePlugin =
        buildBundleDependency(2, 3, MULE_PLUGIN_CLASSIFIER, VERSION, firstMulePluginDependencies).build();

    appMulePluginDependencies.add(firstMulePlugin);
    appMulePluginDependencies.add(secondMulePlugin);

    mavenClient = getMavenClientMock(appDependencies, appMulePluginDependencies);

    AdditionalPluginDependenciesResolver additionalPluginDependenciesResolver = mock(AdditionalPluginDependenciesResolver.class);
    Map<BundleDependency, List<BundleDependency>> additionalPluginDependencies = new HashMap<>();
    additionalPluginDependencies.put(firstMulePlugin, Lists.newArrayList(dependency1));
    when(additionalPluginDependenciesResolver.resolveDependencies(eq(appDependencies), any(Collection.class)))
        .thenReturn(additionalPluginDependencies);

    JarExplorer jarExplorer = mock(JarExplorer.class);
    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        getClassLoaderModelAssemblySpy(mavenClient, additionalPluginDependenciesResolver, jarExplorer);

    File outputDirectory = createFolder();
    File classesDirectory = new File(outputDirectory, CLASSES);
    assertThat(classesDirectory.mkdirs()).isTrue();
    Set<String> packages = new HashSet<>();
    packages.add("org.test");
    Set<String> resources = new HashSet<>();
    resources.add("folder/file.properties");
    when(jarExplorer.explore(classesDirectory.toURI())).thenReturn(new JarInfo(packages, resources));
    ApplicationClassloaderModel applicationClassloaderModel =
        applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class), outputDirectory,
                                                                               mock(ApplicationGAVModel.class), true, empty(),
                                                                               new ArrayList<>());

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies())
        .as("Application dependencies are not the expected")
        .containsAll(Arrays.asList(toArtifact(firstMulePlugin), toArtifact(secondMulePlugin), toArtifact(dependency1),
                                   toArtifact(dependency2), toArtifact(dependency3)));


    assertThat(applicationClassloaderModel.getPackages()).isEqualTo(packages.toArray());
    assertThat(applicationClassloaderModel.getResources()).isEqualTo(resources.toArray());
  }

  @Test
  void getClassLoaderModelWithSharedDependencies() {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1, EMPTY);

    BundleDependency sharedLibrary = buildBundleDependency(2, 2, EMPTY);
    appDependencies.add(dependency1);
    appDependencies.add(sharedLibrary);
    ApplicationClassloaderModel applicationClassloaderModel =
        testSharedDependencies(appDependencies, singletonList(sharedLibrary), empty(), empty());

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies())
        .as("Application contains both libraries")
        .containsAll(Arrays.asList(toArtifact(dependency1), toArtifact(sharedLibrary)));

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies().stream().anyMatch(artifact -> artifact
        .getArtifactCoordinates().getArtifactId().equals(sharedLibrary.getDescriptor().getArtifactId()) &&
        artifact.getArtifactCoordinates().getGroupId().equals(sharedLibrary.getDescriptor().getGroupId()) &&
        artifact.isShared())).as("Application shared library is marked as shared").isTrue();
  }

  @Test
  void getClassLoaderModelWithSharedDependenciesAndProfiles() {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1,
                                                         EMPTY);

    BundleDependency sharedLibrary = buildBundleDependency(2, 2,
                                                           EMPTY);
    appDependencies.add(dependency1);
    appDependencies.add(sharedLibrary);
    ApplicationClassloaderModel applicationClassloaderModel =
        testSharedDependencies(appDependencies, singletonList(sharedLibrary), empty(), Optional.of("Local"));

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies())
        .as("Application contains both libraries")
        .containsAll(Arrays.asList(toArtifact(dependency1), toArtifact(sharedLibrary)));

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies().stream().anyMatch(artifact -> artifact
        .getArtifactCoordinates().getArtifactId().equals(sharedLibrary.getDescriptor().getArtifactId()) &&
        artifact.getArtifactCoordinates().getGroupId().equals(sharedLibrary.getDescriptor().getGroupId()) &&
        artifact.isShared())).as("Application shared library is marked as shared").isTrue();
  }

  @Test
  void getClassLoaderModelWithSharedDependenciesWithTransitiveDependencies() {
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
        testSharedDependencies(appDependencies, singletonList(sharedLibrary), empty(), empty());

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies())
        .as("Application contains all libraries")
        .containsAll(Arrays.asList(toArtifact(dependency1), toArtifact(sharedLibrary),
                                   toArtifact(sharedLibraryTransitiveDependencyLevel1),
                                   toArtifact(sharedLibraryTransitiveDependencyLevel2)));

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies().stream()
        .filter(Artifact::isShared).collect(Collectors.toList()))
            .as("Application contains shared libraries with transitive dependencies")
            .containsAll(Arrays.asList(toArtifact(sharedLibrary),
                                       toArtifact(sharedLibraryTransitiveDependencyLevel1),
                                       toArtifact(sharedLibraryTransitiveDependencyLevel2)));

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies().stream().anyMatch(artifact -> artifact
        .getArtifactCoordinates().getArtifactId().equals(sharedLibrary.getDescriptor().getArtifactId()) &&
        artifact.getArtifactCoordinates().getGroupId().equals(sharedLibrary.getDescriptor().getGroupId()) &&
        artifact.isShared())).as("Application shared library is marked as shared").isTrue();
  }

  private ApplicationClassloaderModel testSharedDependencies(List<BundleDependency> appDependencies,
                                                             List<BundleDependency> sharedLibraries,
                                                             Optional<Model> providedModel, Optional<String> profile) {


    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();

    mavenClient = getMavenClientMock(appDependencies, appMulePluginDependencies);

    when(mavenClient.resolveBundleDescriptorDependencies(eq(false), eq(false), any()))
        .thenReturn(appMulePluginDependencies);

    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        getClassLoaderModelAssemblySpy(mavenClient);

    Model artifactPomModel = providedModel.orElse(createArtifactModel(sharedLibraries, profile));

    doReturn(artifactPomModel).when(applicationClassLoaderModelAssemblerSpy).getPomFile(any());
    List<String> profiles = new ArrayList<>();
    profile.ifPresent(profiles::add);

    try {
      return applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class), createFolder(),
                                                                                    mock(ApplicationGAVModel.class), false,
                                                                                    empty(), profiles);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void noBuildInModelDoesNotFail() {
    testSharedDependencies(emptyList(), emptyList(), Optional.of(new Model()), empty());
  }

  @Test
  void noMavenPluginInModelDoesNotFail() {
    Model model = new Model();
    model.setBuild(new Build());
    testSharedDependencies(emptyList(), emptyList(), Optional.of(model), empty());
  }

  @Test
  void noConfigurationInMavenPluginDoesNotFail() {
    Model model = new Model();
    Build build = new Build();
    Plugin plugin = new Plugin();
    plugin.setGroupId(MULE_MAVEN_PLUGIN_GROUP_ID);
    plugin.setArtifactId(MULE_MAVEN_PLUGIN_ARTIFACT_ID);
    build.addPlugin(plugin);
    model.setBuild(build);
    testSharedDependencies(emptyList(), emptyList(), Optional.of(model), empty());
  }

  private Model createArtifactModel(List<BundleDependency> sharedLibraries, Optional<String> profile) {
    Model artifactPomModel = new Model();
    Plugin plugin = new Plugin();
    plugin.setArtifactId(MULE_MAVEN_PLUGIN_ARTIFACT_ID);
    plugin.setGroupId(MULE_MAVEN_PLUGIN_GROUP_ID);
    Xpp3Dom configuration = new Xpp3Dom("configuration");
    plugin.setConfiguration(configuration);
    Xpp3Dom sharedLibrariesDom = new Xpp3Dom(SHARED_LIBRARIES_FIELD);
    configuration.addChild(sharedLibrariesDom);
    sharedLibraries.forEach(sharedLibraryToAdd -> {
      Xpp3Dom sharedLibraryDom = new Xpp3Dom(SHARED_LIBRARY_FIELD);
      sharedLibrariesDom.addChild(sharedLibraryDom);
      Xpp3Dom groupIdDom = new Xpp3Dom("groupId");
      groupIdDom.setValue(sharedLibraryToAdd.getDescriptor().getGroupId());
      sharedLibraryDom.addChild(groupIdDom);
      Xpp3Dom artifactIdDom = new Xpp3Dom("artifactId");
      artifactIdDom.setValue(sharedLibraryToAdd.getDescriptor().getArtifactId());
      sharedLibraryDom.addChild(artifactIdDom);
    });
    Build b = new Build();
    b.addPlugin(plugin);
    if (profile.isPresent()) {
      Profile p = new Profile();
      p.setId(profile.get());
      p.setBuild(b);
      artifactPomModel.addProfile(p);
    } else {
      artifactPomModel.setBuild(b);
    }
    System.out.println(artifactPomModel);
    return artifactPomModel;
  }

  @Test
  void getClassLoaderModelWithOneDependencyThatIsNotMulePluginTest() {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1,
                                                         EMPTY);
    appDependencies.add(dependency1);

    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();

    mavenClient = getMavenClientMock(appDependencies, appMulePluginDependencies);

    when(mavenClient.resolveBundleDescriptorDependencies(eq(false), eq(false), any())).thenReturn(new ArrayList<>());

    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        getClassLoaderModelAssemblySpy(mavenClient);

    ApplicationClassloaderModel applicationClassloaderModel =
        applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class), mock(ApplicationGAVModel.class));

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies())
        .as("Application dependencies are not the expected")
        .containsAll(singletonList(toArtifact(dependency1)));

    assertThat(applicationClassloaderModel.getMulePluginsClassloaderModels())
        .as("The application should have no mule plugin dependencies")
        .hasSize(0);
  }

  @Test
  void getClassLoaderModelWithOneDependencyThatIsAMulePluginTest() {
    List<BundleDependency> appDependencies = new ArrayList<>();

    BundleDependency mulePluginTransitiveDependency1 =
        buildBundleDependency(1, 1, EMPTY);

    BundleDependency firstMulePlugin =
        buildBundleDependency(2, 3, MULE_PLUGIN_CLASSIFIER, VERSION, singletonList(mulePluginTransitiveDependency1)).build();

    mavenClient = getMavenClientMock(appDependencies, singletonList(firstMulePlugin));

    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        getClassLoaderModelAssemblySpy(mavenClient);

    ApplicationClassloaderModel applicationClassloaderModel =
        applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class), mock(ApplicationGAVModel.class));

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies())
        .as("The class loader model should have one dependency")
        .hasSize(1);

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies())
        .as("Mule plugins are not the expected")
        .containsAll(singletonList(toArtifact(firstMulePlugin)));

    assertThat(applicationClassloaderModel.getMulePluginsClassloaderModels().get(0).getDependencies())
        .as("First mule plugin dependencies are not the expected")
        .containsAll(singletonList(toArtifact(mulePluginTransitiveDependency1)));
  }

  private BundleDependency buildBundleDependency(int groupIdSuffix, int artifactIdSuffix, String classifier) {
    return buildBundleDependency(groupIdSuffix, artifactIdSuffix, classifier, VERSION, emptyList()).build();
  }

  private BundleDependency.Builder buildBundleDependency(int groupIdSuffix, int artifactIdSuffix, String classifier,
                                                         String version, List<BundleDependency> transitiveDependencies) {
    BundleDescriptor bundleDescriptor = buildBundleDescriptor(groupIdSuffix, artifactIdSuffix, classifier, version);
    BundleDependency.Builder builder = new BundleDependency.Builder().setDescriptor(bundleDescriptor);
    if (!classifier.equals(MULE_DOMAIN_CLASSIFIER)) {
      builder.setBundleUri(buildBundleURI(bundleDescriptor));
    }
    transitiveDependencies.stream().forEach(builder::addTransitiveDependency);
    return builder;
  }

  private URI buildBundleURI(BundleDescriptor bundleDescriptor) {
    File bundleFileFolder = new File(temporaryFolder.toFile().getAbsolutePath() + SEPARATOR
        + bundleDescriptor.getGroupId().replace(GROUP_ID_SEPARATOR, SEPARATOR) + bundleDescriptor.getArtifactId() + SEPARATOR
        + bundleDescriptor.getBaseVersion());
    assertThat(bundleFileFolder.mkdirs()).isTrue();


    File bundleFile = new File(bundleFileFolder, bundleDescriptor.getArtifactId() + "-" + bundleDescriptor.getBaseVersion()
        + ".jar");
    try {
      compress(bundleFile,
               get(this.getClass().getClassLoader().getResource("org/mule/tools/api/classloader/model/util/testpackages").toURI())
                   .toFile());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    return bundleFile.toURI();
  }

  private BundleDescriptor buildBundleDescriptor(int groupIdSuffix, int artifactIdSuffix, String classifier, String version) {
    return new BundleDescriptor.Builder().setGroupId(GROUP_ID + groupIdSuffix).setArtifactId(ARTIFACT_ID + artifactIdSuffix)
        .setVersion(version).setBaseVersion(version).setType(TYPE).setClassifier(classifier).build();
  }

  private ApplicationClassLoaderModelAssembler getClassLoaderModelAssemblySpy(MavenClient mavenClient,
                                                                              AdditionalPluginDependenciesResolver additionalPluginDependenciesResolver,
                                                                              JarExplorer jarExplorer) {
    try {
      ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy;
      if (additionalPluginDependenciesResolver != null) {
        applicationClassLoaderModelAssemblerSpy =
            spy(new ApplicationClassLoaderModelAssembler(new ApplicationDependencyResolver(mavenClient),
                                                         new MulePluginClassloaderModelResolver(mavenClient),
                                                         additionalPluginDependenciesResolver, jarExplorer));
      } else {
        applicationClassLoaderModelAssemblerSpy =
            spy(new ApplicationClassLoaderModelAssembler(mavenClient, createFolder()));
      }
      ArtifactCoordinates projectArtifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
      doReturn(new Model()).when(applicationClassLoaderModelAssemblerSpy).getPomFile(any());
      doReturn(projectArtifactCoordinates).when(applicationClassLoaderModelAssemblerSpy).getApplicationArtifactCoordinates(any(),
                                                                                                                           any());
      return applicationClassLoaderModelAssemblerSpy;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private ApplicationClassLoaderModelAssembler getClassLoaderModelAssemblySpy(MavenClient mavenClient) {
    return getClassLoaderModelAssemblySpy(mavenClient, null, new FileJarExplorer());
  }

  private MavenClient getMavenClientMock(List<BundleDependency> appDependencies,
                                         List<BundleDependency> appMulePluginDependencies) {

    MavenClient mavenClient = mock(MuleMavenClient.class);
    appDependencies.addAll(appMulePluginDependencies);
    when(mavenClient.resolveArtifactDependencies(any(File.class), anyBoolean(),
                                                 anyBoolean(), any(Optional.class),
                                                 any(Optional.class), any(Optional.class)))
                                                     .thenReturn(appDependencies);
    when(mavenClient.getEffectiveModel(any(), any()))
        .thenReturn(new MavenPomModelWrapper(new Model()));

    when(mavenClient.getRawPomModel(any(File.class))).thenReturn(new MavenPomModelWrapper(new Model() {

      {
        setPackaging(MULE_APPLICATION_CLASSIFIER);
      }
    }));
    return mavenClient;
  }

  private File createFolder() throws IOException {
    return Files.createDirectories(temporaryFolder.resolve(UUID.randomUUID().toString())).toFile();
  }
}

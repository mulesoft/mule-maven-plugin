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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifact;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.resolver.ApplicationDependencyResolver;
import org.mule.tools.api.classloader.model.resolver.MulePluginClassloaderModelResolver;
import org.mule.tools.api.classloader.model.resolver.RamlClassloaderModelResolver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class ApplicationClassLoaderModelAssemblerTest {



  private static final String USER_REPOSITORY_LOCATION =
      "/Users/muleuser/.m2/repository";
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

  @Test
  public void getClassLoaderModelTest() throws URISyntaxException {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1, StringUtils.EMPTY);
    BundleDependency dependency2 =
        buildBundleDependency(1, 2, StringUtils.EMPTY);
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
        buildBundleDependency(1, 5, StringUtils.EMPTY);
    BundleDependency dependency6 = buildBundleDependency(1, 6,
                                                         StringUtils.EMPTY);
    BundleDependency dependency7 = buildBundleDependency(1, 7, StringUtils.EMPTY);
    BundleDependency dependency8 = buildBundleDependency(1, 8, StringUtils.EMPTY);
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
        applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class));

    assertThat("Application dependencies are not the expected",
               applicationClassloaderModel.getClassLoaderModel().getDependencies(),
               containsInAnyOrder(toArtifact(firstMulePlugin), toArtifact(secondMulePlugin), toArtifact(dependency1),
                                  toArtifact(dependency2), toArtifact(dependency3)));
  }

  @Test
  public void getClassLoaderModelWithOneDependencyThatIsNotMulePluginTest() throws URISyntaxException {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1,
                                                         StringUtils.EMPTY);
    appDependencies.add(dependency1);

    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();

    aetherMavenClientMock = getAetherMavenClientMock(appDependencies, appMulePluginDependencies);

    when(aetherMavenClientMock.resolveBundleDescriptorDependencies(eq(false), eq(false), any())).thenReturn(new ArrayList<>());

    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        getClassLoaderModelAssemblySpy(aetherMavenClientMock);

    ApplicationClassloaderModel applicationClassloaderModel =
        applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class));

    assertThat("Application dependencies are not the expected",
               applicationClassloaderModel.getClassLoaderModel().getDependencies(),
               containsInAnyOrder(toArtifact(dependency1)));

    assertThat("The application should have no mule plugin dependencies",
               applicationClassloaderModel.getMulePluginsClassloaderModels().size(), equalTo(0));
  }

  @Test
  public void getClassLoaderModelWithOneDependencyThatIsAMulePluginTest() throws URISyntaxException, IOException {
    List<BundleDependency> appDependencies = new ArrayList<>();

    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();
    BundleDependency firstMulePlugin =
        buildBundleDependency(2, 3, MULE_PLUGIN_CLASSIFIER);
    appMulePluginDependencies.add(firstMulePlugin);

    aetherMavenClientMock = getAetherMavenClientMock(appDependencies, appMulePluginDependencies);

    List<BundleDependency> firstMulePluginDependencies = new ArrayList<>();
    BundleDependency mulePluginTransitiveDependency1 =
        buildBundleDependency(1, 1, StringUtils.EMPTY);
    firstMulePluginDependencies.add(mulePluginTransitiveDependency1);

    setPluginDependencyinAetherMavenClientMock(firstMulePlugin, firstMulePluginDependencies);

    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        getClassLoaderModelAssemblySpy(aetherMavenClientMock);

    ApplicationClassloaderModel applicationClassloaderModel =
        applicationClassLoaderModelAssemblerSpy.getApplicationClassLoaderModel(mock(File.class));

    assertThat("The class loader model should have one dependency",
               applicationClassloaderModel.getClassLoaderModel().getDependencies().size(),
               equalTo(1));

    assertThat("Mule plugins are not the expected", applicationClassloaderModel.getClassLoaderModel().getDependencies(),
               containsInAnyOrder(toArtifact(firstMulePlugin)));

    assertThat("First mule plugin dependencies are not the expected",
               applicationClassloaderModel.getMulePluginsClassloaderModels().get(0).getDependencies(),
               containsInAnyOrder(toArtifact(mulePluginTransitiveDependency1)));
  }



  private BundleDependency buildBundleDependency(int groupIdSuffix, int artifactIdSuffix, String classifier)
      throws URISyntaxException {
    return buildBundleDependency(groupIdSuffix, artifactIdSuffix, classifier, VERSION);
  }

  private BundleDependency buildBundleDependency(int groupIdSuffix, int artifactIdSuffix, String classifier, String version)
      throws URISyntaxException {
    BundleDescriptor bundleDescriptor = buildBundleDescriptor(groupIdSuffix, artifactIdSuffix, classifier, version);
    URI bundleUri = buildBundleURI(bundleDescriptor);
    return new BundleDependency.Builder().setDescriptor(bundleDescriptor).setBundleUri(bundleUri).build();
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
    ApplicationClassLoaderModelAssembler applicationClassLoaderModelAssemblerSpy =
        spy(new ApplicationClassLoaderModelAssembler(
                                                     new ApplicationDependencyResolver(aetherMavenClientMock),
                                                     new MulePluginClassloaderModelResolver(aetherMavenClientMock),
                                                     new RamlClassloaderModelResolver(aetherMavenClientMock)));
    ArtifactCoordinates projectArtifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
    doReturn(projectArtifactCoordinates).when(applicationClassLoaderModelAssemblerSpy).getApplicationArtifactCoordinates(any());
    return applicationClassLoaderModelAssemblerSpy;
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

    return aetherMavenClientMock;
  }
}

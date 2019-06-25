/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model.resolver;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.ADDITIONAL_DEPENDENCIES_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.ADDITIONAL_PLUGIN_DEPENDENCIES_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.ARTIFACT_ID_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.DEPENDENCY_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.GROUP_ID_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.MULE_EXTENSIONS_PLUGIN_ARTIFACT_ID;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.MULE_EXTENSIONS_PLUGIN_GROUP_ID;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.MULE_MAVEN_PLUGIN_ARTIFACT_ID;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.MULE_MAVEN_PLUGIN_GROUP_ID;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.PLUGIN_ELEMENT;
import static org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver.VERSION_ELEMENT;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.api.model.MavenConfiguration;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class AdditionalPluginDependenciesResolverTest {

  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID = "test.plugin";
  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID = "org.tests.plugins";
  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_VERSION = "1.0.0";
  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_CLASSIFIER = "mule-plugin";
  private static final String DEPENDENCY_X_ARTIFACT_ID = "declaredPomDependencyX10";
  private static final String DEPENDENCY_X_GROUP_ID = "dep.en.den.cy.x";
  private static final String DEPENDENCY_X_VERSION_10 = "1.0.0";
  private static final String DEPENDENCY_X_VERSION_11 = "1.1.0";
  private static final String DEPENDENCY_X_VERSION_20 = "2.0.0";

  private static final Plugin DECLARED_POM_PLUGIN = new Plugin();

  private static final BundleDependency RESOLVED_BUNDLE_PLUGIN;

  static {
    try {
      RESOLVED_BUNDLE_PLUGIN = new BundleDependency.Builder()
          .setDescriptor(new BundleDescriptor.Builder()
              .setArtifactId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID)
              .setGroupId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID)
              .setVersion(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_VERSION)
              .setClassifier(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_CLASSIFIER)
              .build())
          .setBundleUri(new URI("file://nowhere"))

          .build();
    } catch (URISyntaxException e) {
      throw new MuleRuntimeException(e);
    }
  }

  private static ClassLoaderModel resolvedPluginClassLoaderModel;

  private static Dependency declaredPomDependencyX10;
  private static Dependency declaredPomDependencyX11;
  private static Dependency declaredPomDependencyX20;

  private static Artifact dependencyX10Artifact;
  private static Artifact dependencyX11Artifact;
  private static Artifact dependencyX20Artifact;

  private static ArtifactCoordinates dependencyX10ArtifactCoordinates;
  private static ArtifactCoordinates dependencyX11ArtifactCoordinates;
  private static ArtifactCoordinates dependencyX20ArtifactCoordinates;

  private static BundleDependency resolvedDependencyX10;
  private static BundleDependency resolvedDependencyX11;
  private static BundleDependency resolvedDependencyX20;

  private AetherMavenClient mockedMavenClient;
  private MavenConfiguration mockedMavenConfiguration;

  @Rule
  public ExpectedException expectedException = none();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @BeforeClass
  public static void setUpClass() {
    DECLARED_POM_PLUGIN.setGroupId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID);
    DECLARED_POM_PLUGIN.setArtifactId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID);

    resolvedPluginClassLoaderModel = mock(ClassLoaderModel.class);
    when(resolvedPluginClassLoaderModel.getArtifactCoordinates())
        .thenReturn(new ArtifactCoordinates(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID,
                                            PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID,
                                            PLUGIN_WITH_ADDITIONAL_DEPENDENCY_VERSION));

    declaredPomDependencyX10 = mock(Dependency.class);
    declaredPomDependencyX11 = mock(Dependency.class);
    declaredPomDependencyX20 = mock(Dependency.class);

    resolvedDependencyX10 = mock(BundleDependency.class, RETURNS_DEEP_STUBS);
    resolvedDependencyX11 = mock(BundleDependency.class, RETURNS_DEEP_STUBS);
    resolvedDependencyX20 = mock(BundleDependency.class, RETURNS_DEEP_STUBS);

    dependencyX10Artifact = mock(Artifact.class);
    dependencyX11Artifact = mock(Artifact.class);
    dependencyX20Artifact = mock(Artifact.class);

    dependencyX10ArtifactCoordinates = mock(ArtifactCoordinates.class);
    dependencyX11ArtifactCoordinates = mock(ArtifactCoordinates.class);
    dependencyX20ArtifactCoordinates = mock(ArtifactCoordinates.class);

    when(declaredPomDependencyX10.getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(declaredPomDependencyX10.getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(declaredPomDependencyX10.getVersion()).thenReturn(DEPENDENCY_X_VERSION_10);
    when(declaredPomDependencyX10.getType()).thenReturn("dep");

    when(declaredPomDependencyX11.getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(declaredPomDependencyX11.getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(declaredPomDependencyX11.getVersion()).thenReturn(DEPENDENCY_X_VERSION_11);
    when(declaredPomDependencyX11.getType()).thenReturn("dep");

    when(declaredPomDependencyX20.getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(declaredPomDependencyX20.getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(declaredPomDependencyX20.getVersion()).thenReturn(DEPENDENCY_X_VERSION_20);
    when(declaredPomDependencyX20.getType()).thenReturn("dep");

    when(dependencyX10ArtifactCoordinates.getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(dependencyX10ArtifactCoordinates.getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(dependencyX10ArtifactCoordinates.getVersion()).thenReturn(DEPENDENCY_X_VERSION_10);

    when(dependencyX11ArtifactCoordinates.getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(dependencyX11ArtifactCoordinates.getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(dependencyX11ArtifactCoordinates.getVersion()).thenReturn(DEPENDENCY_X_VERSION_11);

    when(dependencyX20ArtifactCoordinates.getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(dependencyX20ArtifactCoordinates.getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(dependencyX20ArtifactCoordinates.getVersion()).thenReturn(DEPENDENCY_X_VERSION_20);

    when(dependencyX10Artifact.getArtifactCoordinates()).thenReturn(dependencyX10ArtifactCoordinates);
    when(dependencyX11Artifact.getArtifactCoordinates()).thenReturn(dependencyX11ArtifactCoordinates);
    when(dependencyX20Artifact.getArtifactCoordinates()).thenReturn(dependencyX20ArtifactCoordinates);

    when(resolvedDependencyX10.getDescriptor().getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(resolvedDependencyX10.getDescriptor().getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(resolvedDependencyX10.getDescriptor().getVersion()).thenReturn(DEPENDENCY_X_VERSION_10);

    when(resolvedDependencyX11.getDescriptor().getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(resolvedDependencyX11.getDescriptor().getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(resolvedDependencyX11.getDescriptor().getVersion()).thenReturn(DEPENDENCY_X_VERSION_11);

    when(resolvedDependencyX20.getDescriptor().getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(resolvedDependencyX20.getDescriptor().getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(resolvedDependencyX20.getDescriptor().getVersion()).thenReturn(DEPENDENCY_X_VERSION_20);
  }

  @Before
  public void setUp() throws IOException {
    mockedMavenClient = mock(AetherMavenClient.class);
    doAnswer(invocationOnMock -> {
      List<BundleDescriptor> dependencies = invocationOnMock.getArgument(0);
      return dependencies.stream()
          .map(dep -> resolveBundleDescriptor(dep))
          .filter(dependencyOptional -> dependencyOptional.isPresent())
          .map(dependencyOptional -> dependencyOptional.get())
          .collect(toList());
    }).when(mockedMavenClient).resolveArtifactDependencies(any(), any(), any());
    when(mockedMavenClient.getEffectiveModel(any(), any())).thenReturn(new Model());
    mockedMavenConfiguration = mock(MavenConfiguration.class);
    when(mockedMavenConfiguration.getLocalMavenRepositoryLocation()).thenReturn(temporaryFolder.newFolder());
    when(mockedMavenClient.getMavenConfiguration()).thenReturn(mockedMavenConfiguration);
  }

  private Optional<BundleDependency> resolveBundleDescriptor(BundleDescriptor bundleDescriptor) {
    if (bundleDescriptor.getArtifactId().equals(DEPENDENCY_X_ARTIFACT_ID) &&
        bundleDescriptor.getGroupId().equals(DEPENDENCY_X_GROUP_ID)) {
      switch (bundleDescriptor.getVersion()) {
        case DEPENDENCY_X_VERSION_10:
          return Optional.of(resolvedDependencyX10);
        case DEPENDENCY_X_VERSION_11:
          return Optional.of(resolvedDependencyX11);
        default:
          return Optional.of(resolvedDependencyX20);
      }
    }
    return empty();
  }

  @Before
  public void resetPlugin() {
    DECLARED_POM_PLUGIN.setAdditionalDependencies(emptyList());
    when(resolvedPluginClassLoaderModel.getDependencies()).thenReturn(emptyList());
  }

  private AdditionalPluginDependenciesResolver createAdditionalPluginDependenciesResolver(List<Plugin> pluginsWithAdditionalDependencies)
      throws IOException {
    return new AdditionalPluginDependenciesResolver(mockedMavenClient, pluginsWithAdditionalDependencies,
                                                    temporaryFolder.newFolder());
  }

  @Test
  public void resolutionFailsIfPluginNotDeclared() throws Exception {
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectMessage("plugin not present");
    createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN)).resolveDependencies(emptyList(), emptyList());
  }

  @Test
  public void resolutionFailsIfPluginClassLoaderModelWasNotCreated() throws Exception {
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectMessage("ClassLoaderModel");
    createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN)).resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN),
                                                                                            emptyList());
  }

  @Test
  public void additionalDependenciesGetResolved() throws IOException {
    DECLARED_POM_PLUGIN.setAdditionalDependencies(of(declaredPomDependencyX10));
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN))
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies, hasEntry(equalTo(RESOLVED_BUNDLE_PLUGIN), hasItem(resolvedDependencyX10)));
  }

  @Test
  public void additionalPluginDependenciesVersionConflictLatestDeclaredRemains() throws Exception {
    // Maven Client will resolve and get the latest declared dependency when they have the same GA (different version)
    doAnswer(invocationOnMock -> {
      List<BundleDescriptor> dependencies = invocationOnMock.getArgument(0);
      return dependencies.stream()
          .map(dep -> resolveBundleDescriptor(dep))
          .filter(dependencyOptional -> dependencyOptional.isPresent())
          .map(dependencyOptional -> dependencyOptional.get())
          .skip(1)
          .collect(toList());
    }).when(mockedMavenClient).resolveArtifactDependencies(any(), any(), any());

    DECLARED_POM_PLUGIN.setAdditionalDependencies(ImmutableList.of(declaredPomDependencyX10, declaredPomDependencyX11));
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN))
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies.size(), is(1));
    assertThat(resolvedAdditionalDependencies, hasKey(equalTo(RESOLVED_BUNDLE_PLUGIN)));
    assertThat(resolvedAdditionalDependencies.get(RESOLVED_BUNDLE_PLUGIN), hasSize(1));
    assertThat(resolvedAdditionalDependencies.get(RESOLVED_BUNDLE_PLUGIN), hasItem(resolvedDependencyX11));
  }

  @Test
  public void additionalDependencyIsNotAddedIfAlreadyAPluginDependency() throws IOException {
    DECLARED_POM_PLUGIN.setAdditionalDependencies(of(declaredPomDependencyX10));
    when(resolvedPluginClassLoaderModel.getDependencies()).thenReturn(of(dependencyX10Artifact));
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN))
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies.isEmpty(), equalTo(true));
  }

  @Test
  public void additionalDependenciesFromMulePluginWithNoBuild() throws IOException {
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(new Model());
  }

  @Test
  public void additionalDependenciesFromMulePluginWithNoPlugin() throws IOException {
    Model model = new Model();
    model.setBuild(new Build());
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(model);
  }

  @Test
  public void additionalDependenciesFromMulePluginWithExtensionsPluginNoConfiguration() throws IOException {
    testAdditionalDependenciesWithNoConfiguration(MULE_EXTENSIONS_PLUGIN_GROUP_ID, MULE_EXTENSIONS_PLUGIN_ARTIFACT_ID);
  }

  @Test
  public void additionalDependenciesFromMulePluginWithApplicationPluginNoConfiguration() throws IOException {
    testAdditionalDependenciesWithNoConfiguration(MULE_MAVEN_PLUGIN_GROUP_ID, MULE_MAVEN_PLUGIN_ARTIFACT_ID);
  }

  @Test
  public void additionalDependenciesFromPluginWithNoAdditionalPluginDependencies() throws IOException {
    Model model = createModelWithConfiguration().getLeft();
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(model);
  }

  @Test
  public void additionalDependenciesFromPluginWithEmptyAdditionalPluginDependencies() throws IOException {
    Pair<Model, Xpp3Dom> pair = createModelWithConfiguration();
    pair.getRight().addChild(new Xpp3Dom(ADDITIONAL_PLUGIN_DEPENDENCIES_ELEMENT));
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(pair.getLeft());
  }

  @Test
  public void additionalDependenciesFromPluginWithEmptyGroupIdAdditionalPluginDependencies() throws IOException {
    testNoAddtionalPluginDependencyBundleDescriptorField(ARTIFACT_ID_ELEMENT, GROUP_ID_ELEMENT);
  }

  @Test
  public void additionalDependenciesFromPluginWithEmptyArtifactIdAdditionalPluginDependencies() throws IOException {
    testNoAddtionalPluginDependencyBundleDescriptorField(GROUP_ID_ELEMENT, ARTIFACT_ID_ELEMENT);
  }

  @Test
  public void nonEmptyAdditionalDependenciesFromPlugin() throws IOException {
    Pair<Model, Xpp3Dom> pair = createModelWithConfiguration();
    Xpp3Dom additionalPluginDependencies = new Xpp3Dom(ADDITIONAL_PLUGIN_DEPENDENCIES_ELEMENT);
    pair.getRight().addChild(additionalPluginDependencies);
    Xpp3Dom pluginChildren = new Xpp3Dom(PLUGIN_ELEMENT);
    additionalPluginDependencies.addChild(pluginChildren);
    Xpp3Dom artifactId = new Xpp3Dom(ARTIFACT_ID_ELEMENT);
    pluginChildren.addChild(artifactId);
    artifactId.setValue(RESOLVED_BUNDLE_PLUGIN.getDescriptor().getArtifactId());
    Xpp3Dom groupId = new Xpp3Dom(GROUP_ID_ELEMENT);
    pluginChildren.addChild(groupId);
    groupId.setValue(RESOLVED_BUNDLE_PLUGIN.getDescriptor().getGroupId());
    Xpp3Dom additionalDependencies = new Xpp3Dom(ADDITIONAL_DEPENDENCIES_ELEMENT);
    pluginChildren.addChild(additionalDependencies);
    addDependency(additionalDependencies, "1");
    addDependency(additionalDependencies, "2");
    reset(mockedMavenClient);
    when(mockedMavenClient.getEffectiveModel(any(), any())).thenReturn(pair.getLeft());

    when(mockedMavenClient.getMavenConfiguration()).thenReturn(mockedMavenConfiguration);

    BundleDependency mockedBundleDependency1 = mock(BundleDependency.class, RETURNS_DEEP_STUBS);
    when(mockedBundleDependency1.getDescriptor().getArtifactId()).thenReturn(ARTIFACT_ID_ELEMENT + "1");
    when(mockedBundleDependency1.getDescriptor().getGroupId()).thenReturn(GROUP_ID_ELEMENT + "1");
    when(mockedBundleDependency1.getDescriptor().getVersion()).thenReturn("1.0.0");

    BundleDependency mockedBundleDependency2 = mock(BundleDependency.class, RETURNS_DEEP_STUBS);
    when(mockedBundleDependency2.getDescriptor().getArtifactId()).thenReturn(ARTIFACT_ID_ELEMENT + "2");
    when(mockedBundleDependency2.getDescriptor().getGroupId()).thenReturn(GROUP_ID_ELEMENT + "2");
    when(mockedBundleDependency2.getDescriptor().getVersion()).thenReturn("1.0.0");

    doAnswer(invocationOnMock -> {
      List<BundleDescriptor> dependencies = invocationOnMock.getArgument(0);
      return dependencies.stream()
          .map(dep -> {
            String artifactId1 = dep.getArtifactId();
            if (artifactId1.equals(ARTIFACT_ID_ELEMENT + "1")) {
              return mockedBundleDependency1;
            } else {
              return mockedBundleDependency2;
            }
          })
          .collect(toList());
    }).when(mockedMavenClient).resolveArtifactDependencies(any(), any(), any());

    when(mockedMavenClient.resolveBundleDescriptorDependencies(eq(false), eq(false), any())).thenReturn(emptyList());
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(emptyList())
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies.size(), is(1));
    List<BundleDependency> dependencies = resolvedAdditionalDependencies.values().iterator().next();
    assertThat(dependencies.size(), is(2));
  }

  private void addDependency(Xpp3Dom additionalDependencies, String suffix) {
    Xpp3Dom additionalDependency1 = new Xpp3Dom(DEPENDENCY_ELEMENT);
    additionalDependencies.addChild(additionalDependency1);
    Xpp3Dom additionalDependency1GroupId = new Xpp3Dom(GROUP_ID_ELEMENT);
    additionalDependency1GroupId.setValue(GROUP_ID_ELEMENT + suffix);
    additionalDependency1.addChild(additionalDependency1GroupId);
    Xpp3Dom additionalDependency1ArtifactId = new Xpp3Dom(ARTIFACT_ID_ELEMENT);
    additionalDependency1ArtifactId.setValue(ARTIFACT_ID_ELEMENT + suffix);
    additionalDependency1.addChild(additionalDependency1ArtifactId);
    Xpp3Dom additionalDependency1Version = new Xpp3Dom(VERSION_ELEMENT);
    additionalDependency1Version.setValue(VERSION_ELEMENT + suffix);
    additionalDependency1.addChild(additionalDependency1Version);
  }

  private void testNoAddtionalPluginDependencyBundleDescriptorField(String partNameWithValue, String partNameWithoutValue)
      throws IOException {
    Pair<Model, Xpp3Dom> pair = createModelWithConfiguration();
    Xpp3Dom additionalPluginDependencies = new Xpp3Dom(ADDITIONAL_PLUGIN_DEPENDENCIES_ELEMENT);
    pair.getRight().addChild(additionalPluginDependencies);
    Xpp3Dom pluginChildren = new Xpp3Dom(PLUGIN_ELEMENT);
    additionalPluginDependencies.addChild(pluginChildren);
    Xpp3Dom artifactId = new Xpp3Dom(partNameWithValue);
    pluginChildren.addChild(artifactId);
    artifactId.setValue("value");
    expectedException.expectMessage(String.format("Expecting child element with not null value %s", partNameWithoutValue));
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(pair.getLeft());
  }

  private void testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(Model model) throws IOException {
    reset(mockedMavenClient);
    when(mockedMavenClient.getEffectiveModel(any(), any())).thenReturn(model);
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(emptyList())
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies.isEmpty(), is(true));
  }

  private Pair<Model, Xpp3Dom> createModelWithConfiguration() {
    Model model = new Model();
    Build build = new Build();
    model.setBuild(build);
    org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
    plugin.setGroupId(MULE_MAVEN_PLUGIN_GROUP_ID);
    plugin.setArtifactId(MULE_MAVEN_PLUGIN_ARTIFACT_ID);
    build.addPlugin(plugin);
    Xpp3Dom configuration = new Xpp3Dom("configuration");
    plugin.setConfiguration(configuration);
    return Pair.of(model, configuration);
  }

  private void testAdditionalDependenciesWithNoConfiguration(String pluginGroupId, String pluginArtifactId) throws IOException {
    Model model = new Model();
    Build build = new Build();
    model.setBuild(build);
    org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
    plugin.setGroupId(pluginGroupId);
    plugin.setArtifactId(pluginArtifactId);
    build.addPlugin(plugin);
    testNoAdditionalDependenciesMulePluginDependencyPomConfiguration(model);
  }

}

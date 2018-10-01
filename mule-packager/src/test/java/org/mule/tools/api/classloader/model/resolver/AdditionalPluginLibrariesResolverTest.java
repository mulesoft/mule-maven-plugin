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
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class AdditionalPluginLibrariesResolverTest {

  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID = "test.plugin";
  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID = "org.tests.plugins";
  private static final String PLUGIN_WITH_ADDITIONAL_DEPENDENCY_VERSION = "1.0.0";
  private static final String DEPENDENCY_X_ARTIFACT_ID = "declaredPomDependencyX";
  private static final String DEPENDENCY_X_GROUP_ID = "dep.en.den.cy.x";
  private static final String DEPENDENCY_X_VERSION = "1.0.0";

  private static final Plugin DECLARED_POM_PLUGIN = new Plugin();

  private static final BundleDependency RESOLVED_BUNDLE_PLUGIN = new BundleDependency.Builder()
      .setDescriptor(new BundleDescriptor.Builder()
          .setArtifactId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID)
          .setGroupId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID)
          .setVersion(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_VERSION)
          .build())
      .build();

  private static ClassLoaderModel resolvedPluginClassLoaderModel;

  private static Dependency declaredPomDependencyX;

  private static Artifact dependencyXArtifact;
  private static ArtifactCoordinates dependencyXArtifactCoodinates;

  private static BundleDependency resolvedDependencyX;

  private static AetherMavenClient mockedMavenClient;


  @Rule
  public ExpectedException expectedException = none();

  @BeforeClass
  public static void setUp() {
    mockedMavenClient = mock(AetherMavenClient.class);

    DECLARED_POM_PLUGIN.setGroupId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID);
    DECLARED_POM_PLUGIN.setArtifactId(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID);

    resolvedPluginClassLoaderModel = mock(ClassLoaderModel.class);
    when(resolvedPluginClassLoaderModel.getArtifactCoordinates())
        .thenReturn(new ArtifactCoordinates(PLUGIN_WITH_ADDITIONAL_DEPENDENCY_GROUP_ID,
                                            PLUGIN_WITH_ADDITIONAL_DEPENDENCY_ARTIFACT_ID,
                                            PLUGIN_WITH_ADDITIONAL_DEPENDENCY_VERSION));

    declaredPomDependencyX = mock(Dependency.class);
    resolvedDependencyX = mock(BundleDependency.class);
    dependencyXArtifact = mock(Artifact.class);
    dependencyXArtifactCoodinates = mock(ArtifactCoordinates.class);

    when(declaredPomDependencyX.getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(declaredPomDependencyX.getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(declaredPomDependencyX.getVersion()).thenReturn(DEPENDENCY_X_VERSION);
    when(declaredPomDependencyX.getType()).thenReturn("dep");
    when(dependencyXArtifactCoodinates.getArtifactId()).thenReturn(DEPENDENCY_X_ARTIFACT_ID);
    when(dependencyXArtifactCoodinates.getGroupId()).thenReturn(DEPENDENCY_X_GROUP_ID);
    when(dependencyXArtifactCoodinates.getVersion()).thenReturn(DEPENDENCY_X_VERSION);
    when(dependencyXArtifact.getArtifactCoordinates()).thenReturn(dependencyXArtifactCoodinates);

    when(mockedMavenClient
        .resolveBundleDescriptor(argThat(descriptor -> descriptor.getArtifactId().equals(declaredPomDependencyX.getArtifactId())
            && descriptor.getGroupId().equals(declaredPomDependencyX.getGroupId())))).thenReturn(resolvedDependencyX);
  }

  @Before
  public void resetPlugin() {
    DECLARED_POM_PLUGIN.setAdditionalDependencies(emptyList());
    when(resolvedPluginClassLoaderModel.getDependencies()).thenReturn(emptyList());
  }

  private AdditionalPluginDependenciesResolver createAdditionalPluginDependenciesResolver(List<Plugin> pluginsWithAdditionalDependencies) {
    return new AdditionalPluginDependenciesResolver(mockedMavenClient, pluginsWithAdditionalDependencies);
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
  public void additionalDependenciesGetResolved() {
    DECLARED_POM_PLUGIN.setAdditionalDependencies(of(declaredPomDependencyX));
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN))
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies, hasEntry(equalTo(RESOLVED_BUNDLE_PLUGIN), hasItem(resolvedDependencyX)));
  }

  @Test
  public void additionalDependencyIsNotAddedIfAlreadyAPluginDependency() {
    DECLARED_POM_PLUGIN.setAdditionalDependencies(of(declaredPomDependencyX));
    when(resolvedPluginClassLoaderModel.getDependencies()).thenReturn(of(dependencyXArtifact));
    Map<BundleDependency, List<BundleDependency>> resolvedAdditionalDependencies =
        createAdditionalPluginDependenciesResolver(of(DECLARED_POM_PLUGIN))
            .resolveDependencies(of(RESOLVED_BUNDLE_PLUGIN), of(resolvedPluginClassLoaderModel));
    assertThat(resolvedAdditionalDependencies.isEmpty(), equalTo(true));
  }

}

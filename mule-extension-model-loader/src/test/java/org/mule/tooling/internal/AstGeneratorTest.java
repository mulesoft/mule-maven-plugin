/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;
import org.mule.runtime.api.artifact.ArtifactCoordinates;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.XmlDslModel;
import org.mule.runtime.api.meta.model.parameter.ParameterizedModel;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.serialization.ArtifactAstSerializer;
import org.mule.runtime.ast.api.serialization.ArtifactAstSerializerProvider;
import org.mule.runtime.extension.api.model.construct.ImmutableConstructModel;
import org.mule.tooling.api.AstGenerator;
import org.mule.tooling.api.ConfigurationException;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import org.junit.jupiter.api.Test;
import org.mule.tooling.api.ExtensionModelLoader;
import org.mule.tooling.api.ExtensionModelLoaderFactory;

class AstGeneratorTest extends MavenClientTest {

  private static final String RUNTIME_VERSION = "4.8.0";

  @TempDir
  protected Path workingDir;
  private final ClassRealm classRealm = mock(ClassRealm.class);
  private final MavenClient mavenClient = mock(MavenClient.class);

  @Test
  void generateASTWithExtensionModels() throws Exception {
    final Pair<AstGenerator, ArtifactAst> elements = getElements("mule-config.xml");
    elements.getLeft().validateAST(elements.getRight());

    assertThat(elements.getRight().topLevelComponents().get(0).getModel(ParameterizedModel.class)).isPresent();
    assertThat(elements.getRight().topLevelComponents().get(0).getModel(ParameterizedModel.class).orElse(null))
        .isInstanceOf(ImmutableConstructModel.class);
  }

  @Test
  void throwConfigurationExceptionIfMuleConfigHasErrors() {
    final Pair<AstGenerator, ArtifactAst> elements = getElements("mule-config2.xml");
    assertThatThrownBy(() -> elements.getLeft().validateAST(elements.getRight()))
        .isExactlyInstanceOf(ConfigurationException.class);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void constructorTests(boolean asApplication) throws MalformedURLException {
    try (MockedStatic<ExtensionModelLoaderFactory> extensionModelLoaderFactory = mockStatic(ExtensionModelLoaderFactory.class)) {
      ExtensionModelLoader extensionModelLoader = mock(ExtensionModelLoader.class);
      PluginResources pluginResources = mock(PluginResources.class);

      when(extensionModelLoader.getRuntimeExtensionModels()).thenReturn(Collections.emptySet());
      when(extensionModelLoader.load(any(BundleDescriptor.class))).thenReturn(pluginResources);
      when(pluginResources.getExportedResources()).thenReturn(Collections.emptyList());
      extensionModelLoaderFactory
          .when(() -> ExtensionModelLoaderFactory.createLoader(nullable(MavenClient.class), nullable(Path.class),
                                                               any(ClassLoader.class), anyString()))
          .thenReturn(extensionModelLoader);

      ////
      BundleDependency bundleDependency = mock(BundleDependency.class);
      URL url = mock(URL.class);
      URI uri = mock(URI.class);
      when(uri.toURL()).thenReturn(url);
      when(bundleDependency.getBundleUri()).thenReturn(uri);
      when(mavenClient.resolveBundleDescriptor(any(BundleDescriptor.class))).thenReturn(bundleDependency);

      doThrow(new RuntimeException()).when(classRealm).addURL(nullable(URL.class));
      createAstGenerator(getArtifacts(), getDependencies(), asApplication, "mule-application");
      ///
      reset(classRealm);
      createAstGenerator(getArtifacts(), getDependencies(), asApplication, "mule-application");
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  void generalTest(int type) throws MalformedURLException {
    Set<ExtensionModel> extensionModels = extensionModels();
    List<String> values = Arrays.asList(
                                        "jar:file:///C:/proj/parser/jar/parser.jar!/test.xml",
                                        "jar:mule-config.xml",
                                        "/mule-config.xml",
                                        "jar::///C:/proj/parser/jar/parser.jar!/test.xml");
    List<URL> resources = IntStream.range(0, 4).mapToObj(index -> {
      URL url = mock(URL.class);
      when(url.toExternalForm()).thenReturn(values.get(index));
      return url;
    }).collect(Collectors.toList());

    try (MockedStatic<ExtensionModelLoaderFactory> extensionModelLoaderFactory = mockStatic(ExtensionModelLoaderFactory.class)) {
      ExtensionModelLoader extensionModelLoader = mock(ExtensionModelLoader.class);
      PluginResources pluginResources = mock(PluginResources.class);

      when(extensionModelLoader.getRuntimeExtensionModels()).thenReturn(Collections.emptySet());
      when(extensionModelLoader.load(any(BundleDescriptor.class))).thenReturn(pluginResources);
      when(pluginResources.getExportedResources()).thenReturn(resources);
      when(pluginResources.getExtensionModels()).thenReturn(extensionModels);
      extensionModelLoaderFactory
          .when(() -> ExtensionModelLoaderFactory.createLoader(nullable(MavenClient.class), nullable(Path.class),
                                                               any(ClassLoader.class), anyString()))
          .thenReturn(extensionModelLoader);
      ////
      URL url = mock(URL.class);
      URI uri = mock(URI.class);
      if (type == 0) {
        when(uri.toURL()).thenReturn(url);
      } else {
        when(uri.toURL()).thenThrow(new MalformedURLException());
      }

      BundleDependency bundleDependency = mock(BundleDependency.class);
      when(bundleDependency.getBundleUri()).thenReturn(uri);
      when(mavenClient.resolveBundleDescriptor(any(BundleDescriptor.class))).thenReturn(bundleDependency);

      createAstGenerator(getArtifacts(), getDependencies(), true, "mule-application");
    }
  }

  @Test
  void serializeTest() {
    InputStream inputStream = mock(InputStream.class);
    try (MockedConstruction<ArtifactAstSerializerProvider> constructor =
        mockConstruction(ArtifactAstSerializerProvider.class, (mock, context) -> {
          ArtifactAstSerializer artifactAstSerializer = mock(ArtifactAstSerializer.class);
          when(artifactAstSerializer.serialize(any(ArtifactAst.class))).thenReturn(inputStream);
          when(mock.getSerializer(anyString(), anyString())).thenReturn(artifactAstSerializer);
        })) {
      assertThat(inputStream).isEqualTo(AstGenerator.serialize(mock(ArtifactAst.class)));
      assertThat(constructor.constructed()).isNotEmpty().hasSize(1);
    }
  }

  private Pair<AstGenerator, ArtifactAst> getElements(String muleConfiguration) {
    try {
      final Path workingPath = Paths.get("src", "test", "resources", "test-project");
      final Path configsBasePath = workingPath.resolve("src/main/mule");
      final File m2Repo = getM2Repo(getM2Home());
      final MavenClient client =
          getMavenClientInstance(getMavenConfiguration(m2Repo, getUserSettings(m2Repo), getSettingsSecurity(m2Repo)));
      final AstGenerator generator =
          new AstGenerator(client, "4.3.0", Collections.emptySet(), workingPath, null, Collections.emptyList());
      final ArtifactAst artifact =
          generator.generateAST("test-project",
                                Collections.singletonList(configsBasePath.resolve(muleConfiguration).toFile().getAbsolutePath()),
                                configsBasePath);
      return Pair.of(generator, artifact);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  private List<Dependency> getDependencies() {
    return IntStream.range(0, 10).mapToObj(index -> {
      Dependency dependency = new Dependency();
      dependency.setGroupId("org.mule");
      dependency.setArtifactId("mule-config" + index);
      dependency.setVersion(RUNTIME_VERSION);
      dependency.setClassifier(index < 2 ? null : "mule-plugin");
      dependency.setType(index < 2 ? null : "jar");
      return dependency;
    }).collect(Collectors.toList());
  }

  private Set<Artifact> getArtifacts() {
    return IntStream.range(0, 10).mapToObj(index -> {
      Artifact artifact = mock(Artifact.class);
      when(artifact.getGroupId()).thenReturn("org.mule");
      when(artifact.getArtifactId()).thenReturn("mule-config" + index);
      when(artifact.getVersion()).thenReturn(RUNTIME_VERSION);
      when(artifact.getClassifier()).thenReturn(index < 4 ? null : "mule-plugin");
      when(artifact.getType()).thenReturn(index < 4 ? null : "jar");
      return artifact;
    }).collect(Collectors.toSet());
  }

  private Set<ExtensionModel> extensionModels() {
    return IntStream.range(0, 2).mapToObj(index -> {
      ArtifactCoordinates artifact = new org.mule.runtime.core.api.artifact.ArtifactCoordinates() {

        @Override
        public String getGroupId() {
          return "org.mule";
        }

        @Override
        public String getArtifactId() {
          return "mule-config" + index;
        }

        @Override
        public String getVersion() {
          return "";
        }
      };

      XmlDslModel xmlDslModel = mock(XmlDslModel.class);
      ExtensionModel extensionModel = mock(ExtensionModel.class);
      doReturn(Optional.of(artifact)).when(extensionModel).getArtifactCoordinates();
      doReturn(xmlDslModel).when(extensionModel).getXmlDslModel();
      return extensionModel;
    }).collect(Collectors.toSet());
  }

  private AstGenerator createAstGenerator(Set<Artifact> dependencies, List<Dependency> directDependencies, Boolean asApplication,
                                          String classifier) {
    return new AstGenerator(mavenClient, RUNTIME_VERSION, dependencies, workingDir, classRealm, directDependencies, asApplication,
                            classifier);
  }
}

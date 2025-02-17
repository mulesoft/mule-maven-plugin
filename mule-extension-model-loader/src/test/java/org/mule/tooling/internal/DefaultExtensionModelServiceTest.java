package org.mule.tooling.internal;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mule.maven.pom.parser.api.model.MavenPomModel;
import org.mule.maven.pom.parser.internal.model.MavenModelBuilderImpl;
import org.mule.maven.pom.parser.internal.util.FileUtils;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.container.api.ModuleRepository;
import org.mule.runtime.core.api.util.UUID;
import org.mule.runtime.deployment.model.api.plugin.resolver.PluginDependenciesResolver;
import org.mule.runtime.module.artifact.activation.api.classloader.ArtifactClassLoaderResolver;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.MuleDeployableArtifactClassLoader;
import org.mule.runtime.module.artifact.api.descriptor.*;
import org.mule.runtime.module.deployment.impl.internal.application.ApplicationDescriptorFactory;
import org.mule.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;
import org.mule.tooling.api.ToolingException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class DefaultExtensionModelServiceTest {

  private final BundleDescriptor bundleDescriptor =
      new BundleDescriptor.Builder().setGroupId(UUID.getUUID()).setArtifactId(UUID.getUUID()).setVersion(UUID.getUUID()).setClassifier("mule-plugin").build();
  private final MuleArtifactResourcesRegistry resourcesRegistry = mock(MuleArtifactResourcesRegistry.class);
  private final DefaultExtensionModelService extensionModelService = new DefaultExtensionModelService(resourcesRegistry);

  @BeforeEach
  void setUp() {
    reset(resourcesRegistry);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void getPomModelFromJarTest(boolean withException) {
    Model model = mock(Model.class);
    try (MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class);
        MockedStatic<org.mule.maven.client.internal.util.FileUtils> fileUtilsMock =
            mockStatic(org.mule.maven.client.internal.util.FileUtils.class);
        MockedConstruction<MavenXpp3Reader> reader = mockConstruction(MavenXpp3Reader.class, (mock, context) -> {
          if (withException) {
            when(mock.read(any(InputStream.class))).thenThrow(new IOException());
          } else {
            when(mock.read(any(InputStream.class))).thenReturn(model);
          }
        })) {
      File artifact = mock(File.class);
      URL artifactURL = mock(URL.class);
      fileUtils.when(() -> FileUtils.getPomUrlFromJar(any(File.class))).thenReturn(artifactURL);
      fileUtilsMock.when(() -> org.mule.maven.client.internal.util.FileUtils.loadFileContentFrom(any(URL.class)))
          .thenReturn(Optional.of("".getBytes()));
      //
      if (withException) {
        assertThatThrownBy(() -> DefaultExtensionModelService.getPomModelFromJar(artifact))
            .isInstanceOf(ArtifactDescriptorCreateException.class).hasMessageContaining("There was an issue reading")
            .hasMessageContaining("for the artifact");
      } else {
        assertThat(model).isEqualTo(DefaultExtensionModelService.getPomModelFromJar(artifact));
      }
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  void loadExtensionDataTest(int type) throws IOException {
    File artifact = mock(File.class);
    ApplicationDescriptorFactory applicationDescriptorFactory = mock(ApplicationDescriptorFactory.class);
    ArtifactPluginDescriptorLoader loader = mock(ArtifactPluginDescriptorLoader.class);
    ArtifactPluginDescriptor artifactPluginDescriptor = mock(ArtifactPluginDescriptor.class);
    ApplicationDescriptor applicationDescriptor = mock(ApplicationDescriptor.class);
    ArtifactClassLoaderResolver artifactClassLoaderResolverMock = mock(ArtifactClassLoaderResolver.class);
    MuleDeployableArtifactClassLoader muleDeployableArtifactClassLoader = mock(MuleDeployableArtifactClassLoader.class);
    ClassLoaderConfiguration classLoaderConfiguration = mock(ClassLoaderConfiguration.class);
    Set<String> localResources = new HashSet<String>();
    localResources.add("file.dwl");
    when(classLoaderConfiguration.getLocalResources()).thenReturn(localResources);
    when(applicationDescriptor.getClassLoaderConfiguration()).thenReturn(classLoaderConfiguration);
    if (type == 0) {
      when(loader.load(any(File.class))).thenThrow(new IOException());
    } else {

      when(artifactPluginDescriptor.getBundleDescriptor()).thenReturn(bundleDescriptor);
      when(loader.load(any(File.class))).thenReturn(artifactPluginDescriptor);
    }

    when(applicationDescriptorFactory.createArtifact(any(File.class), any(Optional.class), any(MuleApplicationModel.class)))
        .thenReturn(applicationDescriptor);
    when(resourcesRegistry.getArtifactPluginDescriptorLoader()).thenReturn(loader);
    when(resourcesRegistry.getApplicationDescriptorFactory()).thenReturn(applicationDescriptorFactory);
    when(resourcesRegistry.getPluginDependenciesResolver()).thenReturn(mock(PluginDependenciesResolver.class));
    when(muleDeployableArtifactClassLoader.getArtifactPluginClassLoaders()).thenReturn(Collections.emptyList());
    when(artifactClassLoaderResolverMock.createApplicationClassLoader(any(ApplicationDescriptor.class), any(Supplier.class)))
        .thenReturn(muleDeployableArtifactClassLoader);


    //
    if (type == 0) {
      assertThatThrownBy(() -> extensionModelService.loadExtensionData(artifact))
          .isInstanceOf(ToolingException.class).hasMessageContaining("Error while loading ExtensionModel for plugin: ");
    } else {
      Model model = mock(Model.class);
      try (MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class);
          MockedStatic<ArtifactClassLoaderResolver> artifactClassLoader = mockStatic(ArtifactClassLoaderResolver.class);
          //
          MockedStatic<org.mule.maven.client.internal.util.FileUtils> fileUtilsMock =
              mockStatic(org.mule.maven.client.internal.util.FileUtils.class);
          //
          MockedConstruction<MavenXpp3Reader> reader = mockConstruction(MavenXpp3Reader.class, (mock, context) -> {
            when(mock.read(any(InputStream.class))).thenReturn(model);
          });
          //
          MockedConstruction<MavenModelBuilderImpl> builder = mockConstruction(MavenModelBuilderImpl.class, (mock, context) -> {
            MavenPomModel mavenPomModel = mock(MavenPomModel.class);
            when(mavenPomModel.getGroupId()).thenReturn(bundleDescriptor.getGroupId());
            when(mavenPomModel.getArtifactId()).thenReturn(bundleDescriptor.getArtifactId());
            when(mavenPomModel.getVersion()).thenReturn(bundleDescriptor.getVersion());
            when(mock.getModel()).thenReturn(mavenPomModel);
          })) {
        URL artifactURL = mock(URL.class);
        artifactClassLoader
            .when(() -> ArtifactClassLoaderResolver.classLoaderResolver(any(ArtifactClassLoader.class),
                                                                        any(ModuleRepository.class), any(Function.class)))
            .thenReturn(artifactClassLoaderResolverMock);
        fileUtils.when(() -> FileUtils.getPomUrlFromJar(any(File.class))).thenReturn(artifactURL);
        fileUtilsMock.when(() -> org.mule.maven.client.internal.util.FileUtils.loadFileContentFrom(any(URL.class)))
            .thenReturn(Optional.of("".getBytes()));
        assertThatThrownBy(() -> extensionModelService.loadExtensionData(artifact))
            .isInstanceOf(ToolingException.class).hasMessageContaining("Couldn't find plugin descriptor:");
      }
    }
  }

  @Test
  void readBundleDescriptorTest() throws IOException {
    File pluginFile = mock(File.class);
    ArtifactPluginDescriptorLoader loader = mock(ArtifactPluginDescriptorLoader.class);
    ArtifactPluginDescriptor artifactPluginDescriptor = mock(ArtifactPluginDescriptor.class);

    when(resourcesRegistry.getArtifactPluginDescriptorLoader()).thenReturn(loader);
    when(loader.load(any(File.class))).thenReturn(artifactPluginDescriptor);
    when(artifactPluginDescriptor.getBundleDescriptor()).thenReturn(bundleDescriptor);

    org.mule.maven.pom.parser.api.model.BundleDescriptor descriptor = extensionModelService.readBundleDescriptor(pluginFile);

    assertThat(bundleDescriptor.getGroupId()).isEqualTo(descriptor.getGroupId());
    assertThat(bundleDescriptor.getArtifactId()).isEqualTo(descriptor.getArtifactId());
    assertThat(bundleDescriptor.getVersion()).isEqualTo(descriptor.getVersion());
    assertThat(bundleDescriptor.getClassifier()).isEqualTo(descriptor.getClassifier());
    assertThat("jar").isEqualTo(descriptor.getType());
  }
}

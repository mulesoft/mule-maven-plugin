/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.internal;

import static java.util.Optional.of;
import static org.mule.maven.client.internal.util.FileUtils.loadFileContentFrom;
import static org.mule.maven.pom.parser.api.model.MavenModelBuilderProvider.discoverProvider;
import static org.mule.maven.pom.parser.internal.util.FileUtils.getPomUrlFromJar;
import static org.mule.runtime.api.deployment.meta.Product.MULE;
import static org.mule.runtime.container.api.ContainerClassLoaderProvider.createContainerClassLoader;
import static org.mule.runtime.container.api.ModuleRepository.createModuleRepository;
import static org.mule.runtime.core.api.config.MuleManifest.getProductVersion;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static org.mule.runtime.core.api.util.UUID.getUUID;
import static org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptorConstants.MULE_LOADER_ID;
import static org.mule.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelDiscoverer.defaultExtensionModelDiscoverer;
import static org.mule.runtime.core.api.extension.provider.RuntimeExtensionModelProvider.discoverRuntimeExtensionModels;
import static org.mule.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor.MULE_ARTIFACT_PATH_INSIDE_JAR;
import static org.mule.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor.MULE_PLUGIN_POM;
import static org.mule.runtime.module.deployment.impl.internal.maven.AbstractMavenClassLoaderConfigurationLoader.CLASSLOADER_MODEL_MAVEN_REACTOR_RESOLVER;
import static org.mule.runtime.module.extension.internal.ExtensionProperties.DISABLE_COMPONENT_IGNORE;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.toSet;

import static com.google.common.collect.ImmutableSet.copyOf;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.deleteQuietly;

import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.mule.maven.client.api.MavenReactorResolver;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;
import org.mule.maven.pom.parser.api.model.MavenModelBuilder;
import org.mule.maven.pom.parser.api.model.MavenModelBuilderProvider;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.container.api.ModuleRepository;
import org.mule.runtime.module.artifact.activation.api.classloader.ArtifactClassLoaderResolver;
import org.mule.runtime.module.artifact.activation.api.extension.discovery.ExtensionDiscoveryRequest;
import org.mule.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelDiscoverer;
import org.mule.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import org.mule.runtime.module.artifact.api.classloader.MuleDeployableArtifactClassLoader;
import org.mule.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptorCreateException;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import org.mule.tooling.api.ExtensionModelService;
import org.mule.tooling.api.ToolingException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ExtensionModelService}.
 *
 * @since 4.0
 */
public class DefaultExtensionModelService implements ExtensionModelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExtensionModelService.class);

  private static final String MULE_APPLICATION = "mule-application";
  private static final String MAVEN_MODEL_VERSION = "4.0.0";

  /**
   * Returns the {@link Model} from a given artifact folder
   *
   * @param artifactFile file containing the artifact content.
   * @return the {@link Model} from the {@value ArtifactPluginDescriptor#MULE_PLUGIN_POM} file if available
   * @throws ArtifactDescriptorCreateException if the artifact jar does not contain a
   *                                           {@value ArtifactPluginDescriptor#MULE_PLUGIN_POM} file or the file can' be loaded
   */
  public static Model getPomModelFromJar(File artifactFile) {
    String pomFilePath = MULE_ARTIFACT_PATH_INSIDE_JAR + "/" + MULE_PLUGIN_POM;
    try {
      MavenXpp3Reader reader = new MavenXpp3Reader();
      return reader.read(new ByteArrayInputStream(loadFileContentFrom(getPomUrlFromJar(artifactFile)).get()));
    } catch (IOException | XmlPullParserException e) {
      throw new ArtifactDescriptorCreateException(format("There was an issue reading '%s' for the artifact '%s'",
              pomFilePath, artifactFile.getAbsolutePath()),
              e);
    }
  }

  private final MuleArtifactResourcesRegistry muleArtifactResourcesRegistry;

  private final List<ExtensionModel> runtimeExtensionModels = new ArrayList<>();

  public DefaultExtensionModelService(MuleArtifactResourcesRegistry muleArtifactResourcesRegistry) {
    requireNonNull(muleArtifactResourcesRegistry, "muleArtifactResourcesRegistry cannot be null");

    this.muleArtifactResourcesRegistry = muleArtifactResourcesRegistry;
    this.runtimeExtensionModels.addAll(discoverRuntimeExtensionModels());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExtensionModel> loadRuntimeExtensionModels() {
    return runtimeExtensionModels;
  }

  @Override
  public PluginResources loadExtensionData(File pluginJarFile) {
    long startTime = nanoTime();
    org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor bundleDescriptor =
        readArtifactPluginDescriptor(pluginJarFile).getBundleDescriptor();

    Map<String, Object> classLoaderModelAttributes = new HashMap<>();
    classLoaderModelAttributes
        .put(CLASSLOADER_MODEL_MAVEN_REACTOR_RESOLVER,
                new PluginFileMavenReactor(bundleDescriptor, pluginJarFile, muleArtifactResourcesRegistry.getWorkingDirectory()));

    BundleDescriptor pluginDescriptor = new BundleDescriptor.Builder()
        .setGroupId(bundleDescriptor.getGroupId())
        .setGroupId(bundleDescriptor.getGroupId())
        .setArtifactId(bundleDescriptor.getArtifactId())
        .setVersion(bundleDescriptor.getVersion())
        .setClassifier(bundleDescriptor.getClassifier().orElse(null))
        .build();
    PluginResources extensionInformationOptional = withTemporaryApplication(pluginDescriptor, classLoaderModelAttributes, this::loadExtensionData, null);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Extension model for {} loaded in {}ms", pluginJarFile, NANOSECONDS.toMillis(nanoTime() - startTime));
    }

    return extensionInformationOptional;
  }

  static class PluginFileMavenReactor implements MavenReactorResolver {

    private static final String POM_XML = "pom.xml";
    private static final String POM = "pom";

    private final org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor descriptor;
    private final File mulePluginJarFile;
    private final File temporaryFolder;

    public PluginFileMavenReactor(org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor descriptor,
                                  File mulePluginJarFile, File workingDirectory) {
      this.descriptor = descriptor;
      this.mulePluginJarFile = mulePluginJarFile;

      this.temporaryFolder = new File(workingDirectory, getUUID());
      this.temporaryFolder.mkdirs();

      Model model = getPomModelFromJar(mulePluginJarFile);
      MavenXpp3Writer writer = new MavenXpp3Writer();
      try (FileOutputStream outputStream = new FileOutputStream(new File(temporaryFolder, POM_XML))) {
        writer.write(outputStream, model);
      } catch (IOException e) {
        throw new MuleRuntimeException(e);
      }

    }

    @Override
    public File findArtifact(BundleDescriptor bundleDescriptor) {
      if (checkArtifact(bundleDescriptor)) {
        if (bundleDescriptor.getType().equals(POM)) {
          return new File(temporaryFolder, POM_XML);
        } else {
          return mulePluginJarFile;
        }
      }
      return null;
    }

    @Override
    public List<String> findVersions(BundleDescriptor bundleDescriptor) {
      if (checkArtifact(bundleDescriptor)) {
        return singletonList(descriptor.getVersion());
      }
      return emptyList();
    }

    private boolean checkArtifact(BundleDescriptor bundleDescriptor) {
      return descriptor.getGroupId().equals(bundleDescriptor.getGroupId())
          && descriptor.getArtifactId().equals(bundleDescriptor.getArtifactId())
          && descriptor.getVersion().equals(bundleDescriptor.getVersion());
    }


    public void dispose() {
      try {
        deleteDirectory(temporaryFolder);
      } catch (IOException e) {
        // Nothing to do...
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BundleDescriptor readBundleDescriptor(File pluginFile) {
    org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor bundleDescriptor =
        readArtifactPluginDescriptor(pluginFile).getBundleDescriptor();
    return new BundleDescriptor.Builder()
        .setGroupId(bundleDescriptor.getGroupId())
        .setArtifactId(bundleDescriptor.getArtifactId())
        .setBaseVersion(bundleDescriptor.getVersion())
        .setVersion(bundleDescriptor.getVersion())
        .setType(bundleDescriptor.getType())
        .setClassifier(bundleDescriptor.getClassifier().orElse(null))
        .build();
  }

  private ArtifactPluginDescriptor readArtifactPluginDescriptor(File pluginFile) {
    try {
      ArtifactPluginDescriptor artifactPluginDescriptor;
      artifactPluginDescriptor = muleArtifactResourcesRegistry.getArtifactPluginDescriptorLoader().load(pluginFile);
      return artifactPluginDescriptor;
    } catch (Exception e) {
      throw new ToolingException("Error while loading ExtensionModel for plugin: " + pluginFile.getAbsolutePath(), e);
    }
  }

  @Override
  public PluginResources loadExtensionData(BundleDescriptor pluginDescriptor, MuleVersion muleVersion) {
    long startTime = nanoTime();
    PluginResources extensionInformation = withTemporaryApplication(pluginDescriptor, emptyMap(), this::loadExtensionData, muleVersion);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Extension model for {} loaded in {}ms", pluginDescriptor, NANOSECONDS.toMillis(nanoTime() - startTime));
    }

    return extensionInformation;
  }

  private PluginResources withTemporaryApplication(BundleDescriptor pluginDescriptor,
                                                   Map<String, Object> classLoaderModelLoaderAttributes,
                                                   TemporaryApplicationFunction action,
                                                   MuleVersion muleVersion) {
    String uuid = getUUID();
    String applicationName = uuid + "-extension-model-temp-app";
    File applicationFolder = new File(muleArtifactResourcesRegistry.getWorkingDirectory(), applicationName);
    try {
      createPomFile(pluginDescriptor, uuid, applicationFolder);
      MuleApplicationModel muleApplicationModel = new MuleApplicationModel.MuleApplicationModelBuilder()
          .setMinMuleVersion(muleVersion != null ? muleVersion.toString() : getProductVersion())
          .setName(applicationName)
          .setRequiredProduct(MULE)
          .withBundleDescriptorLoader(new MuleArtifactLoaderDescriptor(MULE_LOADER_ID, emptyMap()))
          .withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(MULE_LOADER_ID, classLoaderModelLoaderAttributes))
          .build();
      ApplicationDescriptor applicationDescriptor = muleArtifactResourcesRegistry.getApplicationDescriptorFactory()
          .createArtifact(applicationFolder, empty(), muleApplicationModel);

      ModuleRepository moduleRepository = createModuleRepository(ArtifactClassLoaderResolver.class
          .getClassLoader(), createTempDir());
      ArtifactClassLoaderResolver artifactClassLoaderResolver = ArtifactClassLoaderResolver
          .classLoaderResolver(createContainerClassLoader(moduleRepository), moduleRepository, (empty) -> applicationFolder);

      muleArtifactResourcesRegistry.getPluginDependenciesResolver()
          .resolve(emptySet(), new ArrayList<>(applicationDescriptor.getPlugins()), false);

      MuleDeployableArtifactClassLoader artifactClassLoader =
          artifactClassLoaderResolver.createApplicationClassLoader(applicationDescriptor,
                                                                   muleArtifactResourcesRegistry::getContainerArtifactClassLoader);

      try {
        ArtifactPluginDescriptor artifactPluginDescriptor = artifactClassLoader.getArtifactPluginClassLoaders().stream()
            .filter(artifactPluginClassLoader -> artifactPluginClassLoader.getArtifactDescriptor().getBundleDescriptor()
                .getGroupId()
                .equals(pluginDescriptor.getGroupId())
                && artifactPluginClassLoader.getArtifactDescriptor().getBundleDescriptor().getArtifactId()
                    .equals(pluginDescriptor.getArtifactId()))
            .findFirst().orElseThrow(() -> new IllegalStateException(format("Couldn't find plugin descriptor: %s",
                                                                            pluginDescriptor)))
            .getArtifactDescriptor();

        return action.call(artifactPluginDescriptor, artifactClassLoader, pluginDescriptor.getProperties());
      } catch (Exception e) {
        throw new ToolingException(e);
      } finally {
        if (artifactClassLoader != null) {
          artifactClassLoader.dispose();
        }
      }
    } catch (ToolingException e) {
      throw e;
    } catch (Exception e) {
      throw new ToolingException(e);
    } finally {
      deleteQuietly(applicationFolder);
    }
  }

  private File createTempDir() throws IOException {
    File tempFolder = createTempDirectory(null).toFile();
    File moduleDiscovererTemporaryFolder = new File(tempFolder, ".moduleDiscoverer");
    if (!moduleDiscovererTemporaryFolder.mkdir()) {
      throw new IOException("Error while generating class loaders, cannot create directory "
          + moduleDiscovererTemporaryFolder.getAbsolutePath());
    }
    return moduleDiscovererTemporaryFolder;
  }

  private void createPomFile(BundleDescriptor pluginDescriptor, String uuid, File applicationFolder) {
    MavenModelBuilderProvider mavenModelBuilderProvider = discoverProvider();
    MavenModelBuilder model = mavenModelBuilderProvider
            .createMavenModelBuilder(uuid, uuid, getProductVersion(), of(MAVEN_MODEL_VERSION), of(MULE_APPLICATION));

    model.addDependency(new BundleDependency.Builder().setBundleDescriptor(pluginDescriptor).build());

    Properties pomProperties = new Properties();
    pomProperties.setProperty("groupId", model.getModel().getGroupId());
    pomProperties.setProperty("artifactId", model.getModel().getArtifactId());
    pomProperties.setProperty("version", model.getModel().getVersion());

    model.createDeployablePomFile(applicationFolder.toPath());
    model.createDeployablePomProperties(applicationFolder.toPath(), pomProperties);
  }

  @FunctionalInterface
  interface TemporaryApplicationFunction {

    PluginResources call(ArtifactPluginDescriptor artifactPluginDescriptor,
                         MuleDeployableArtifactClassLoader artifactClassLoader,
                         Map<String, String> properties);
  }

  private PluginResources loadExtensionData(ArtifactPluginDescriptor artifactPluginDescriptor,
                                            MuleDeployableArtifactClassLoader artifactClassLoader,
                                            Map<String, String> properties) {
    try {
      ArrayList<URL> resources = new ArrayList<>();
      artifactPluginDescriptor.getClassLoaderConfiguration().getExportedResources().forEach(resource -> {
        if (artifactClassLoader.getParent().getResource(resource) != null) {
          resources.add(artifactClassLoader.getParent().getResource(resource));
        }
      });
      ExtensionModelLoaderRepository extensionModelLoaderRepository =
          ExtensionModelLoaderRepository.getExtensionModelLoaderManager();
      startIfNeeded(extensionModelLoaderRepository);
      final Set<ExtensionModel> loadedExtensionInformation =
          discoverPluginsExtensionModel(artifactClassLoader, extensionModelLoaderRepository, properties);
      return new PluginResources(loadedExtensionInformation, resources);
    } catch (Exception e) {
      throw new ToolingException(e);
    } finally {
      if (artifactClassLoader != null) {
        artifactClassLoader.dispose();
      }
    }
  }

  private Set<ExtensionModel> discoverPluginsExtensionModel(MuleDeployableArtifactClassLoader artifactClassLoader,
                                                            ExtensionModelLoaderRepository extensionModelLoaderRepository,
                                                            Map<String, String> properties) {
    Set<ArtifactPluginDescriptor> artifactPluginDescriptors =
        artifactClassLoader.getArtifactPluginClassLoaders().stream()
            .map(a -> effectiveModel(properties, a.getArtifactDescriptor())).collect(toSet());
    ExtensionModelDiscoverer extensionModelDiscoverer = defaultExtensionModelDiscoverer(artifactClassLoader, extensionModelLoaderRepository);
    ExtensionDiscoveryRequest request = ExtensionDiscoveryRequest.builder()
        .setArtifactPlugins(artifactPluginDescriptors)
        .setParentArtifactExtensions(copyOf(loadRuntimeExtensionModels()))
        .build();
    return extensionModelDiscoverer.discoverPluginsExtensionModels(request);
  }

  private ArtifactPluginDescriptor effectiveModel(Map<String, String> properties, ArtifactPluginDescriptor artifactDescriptor) {
    if (Boolean.parseBoolean(properties.getOrDefault(DISABLE_COMPONENT_IGNORE, "false"))) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info(format("Loading effective model for '%s'", artifactDescriptor.getBundleDescriptor()));
      }
      artifactDescriptor.getExtensionModelDescriptorProperty()
          .ifPresent(extensionModelDescriptorProperty -> extensionModelDescriptorProperty
              .addAttributes(ImmutableMap.of(DISABLE_COMPONENT_IGNORE, TRUE)));
    }
    return artifactDescriptor;
  }


}

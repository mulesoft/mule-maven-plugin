package org.mule.tooling.internal;

import static com.google.common.collect.ImmutableSet.copyOf;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.valueOf;
import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.mule.runtime.api.deployment.meta.Product.MULE;
import static org.mule.runtime.core.api.config.MuleManifest.getProductVersion;
import static org.mule.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static org.mule.runtime.core.api.util.UUID.getUUID;
import static org.mule.runtime.deployment.model.api.artifact.ArtifactDescriptorConstants.MULE_LOADER_ID;
import static org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor.MULE_PLUGIN_CLASSIFIER;
import static org.mule.runtime.module.deployment.impl.internal.maven.AbstractMavenClassLoaderModelLoader.CLASSLOADER_MODEL_MAVEN_REACTOR_RESOLVER;
import static org.mule.runtime.module.deployment.impl.internal.maven.MavenUtils.createDeployablePomFile;
import static org.mule.runtime.module.deployment.impl.internal.maven.MavenUtils.createDeployablePomProperties;
import static org.mule.runtime.module.deployment.impl.internal.maven.MavenUtils.getPomModelFromJar;
import static org.mule.runtime.module.extension.internal.ExtensionProperties.DISABLE_COMPONENT_IGNORE;
import org.mule.maven.client.api.MavenReactorResolver;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.deployment.model.api.application.ApplicationDescriptor;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor;
import org.mule.runtime.deployment.model.internal.tooling.ToolingApplicationClassLoaderBuilder;
import org.mule.runtime.deployment.model.internal.tooling.ToolingArtifactClassLoader;
import org.mule.runtime.extension.api.dsl.syntax.resources.spi.ExtensionSchemaGenerator;
import org.mule.runtime.extension.api.persistence.ExtensionModelJsonSerializer;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.DeployableArtifactClassLoaderFactory;
import org.mule.runtime.module.artifact.api.classloader.MuleDeployableArtifactClassLoader;
import org.mule.runtime.module.deployment.impl.internal.artifact.ExtensionModelDiscoverer;
import org.mule.runtime.module.deployment.impl.internal.plugin.MuleExtensionModelLoaderManager;
import org.mule.runtime.module.extension.internal.capability.xml.schema.DefaultExtensionSchemaGenerator;
import org.mule.tooling.api.ExtensionModelService;
import org.mule.tooling.api.LoadedExtensionInformation;
import org.mule.tooling.api.ToolingException;

import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.maven.model.Dependency;
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

  private static final String JAR = "jar";
  private static final String MULE_APPLICATION = "mule-application";
  private static final String MAVEN_MODEL_VERSION = "4.0.0";

  private ExtensionModelDiscoverer extensionModelDiscoverer = new ExtensionModelDiscoverer();
  private MuleArtifactResourcesRegistry muleArtifactResourcesRegistry;
  private ExtensionSchemaGenerator schemaGenerator = new DefaultExtensionSchemaGenerator();

  private List<ExtensionModel> runtimeExtensionModels = new ArrayList<>();

  public DefaultExtensionModelService(MuleArtifactResourcesRegistry muleArtifactResourcesRegistry) {
    requireNonNull(muleArtifactResourcesRegistry, "muleArtifactResourcesRegistry cannot be null");

    this.muleArtifactResourcesRegistry = muleArtifactResourcesRegistry;
    this.runtimeExtensionModels.addAll(new ArrayList<>(extensionModelDiscoverer.discoverRuntimeExtensionModels()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExtensionModel> loadRuntimeExtensionModels() {
    return runtimeExtensionModels;
  }

  @Override
  public Optional<LoadedExtensionInformation> loadExtensionData(File pluginJarFile) {
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
    Optional<LoadedExtensionInformation> extensionInformationOptional =
        withTemporaryApplication(pluginDescriptor, classLoaderModelAttributes,
                                 (artifactPluginDescriptor,
                                  toolingArtifactClassLoader,
                                  properties) -> loadExtensionData(artifactPluginDescriptor,
                                                                   toolingArtifactClassLoader,
                                                                   properties));
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Extension model for {} loaded in {}ms", pluginJarFile, NANOSECONDS.toMillis(nanoTime() - startTime));
    }

    return extensionInformationOptional;
  }

  class PluginFileMavenReactor implements MavenReactorResolver {

    private static final String POM_XML = "pom.xml";
    private static final String POM = "pom";

    private org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor descriptor;
    private File mulePluginJarFile;
    private File temporaryFolder;

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
    public File findArtifact(org.mule.maven.client.api.model.BundleDescriptor bundleDescriptor) {
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
    public List<String> findVersions(org.mule.maven.client.api.model.BundleDescriptor bundleDescriptor) {
      if (checkArtifact(bundleDescriptor)) {
        return singletonList(descriptor.getVersion());
      }
      return emptyList();
    }

    private boolean checkArtifact(org.mule.maven.client.api.model.BundleDescriptor bundleDescriptor) {
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
  public Optional<LoadedExtensionInformation> loadExtensionData(BundleDescriptor pluginDescriptor) {
    long startTime = nanoTime();
    Optional<LoadedExtensionInformation> extensionInformationOptional = withTemporaryApplication(pluginDescriptor, emptyMap(),
                                                                                                 (artifactPluginDescriptor,
                                                                                                  toolingArtifactClassLoader,
                                                                                                  properties) -> loadExtensionData(artifactPluginDescriptor,
                                                                                                                                   toolingArtifactClassLoader,
                                                                                                                                   properties));
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Extension model for {} loaded in {}ms", pluginDescriptor, NANOSECONDS.toMillis(nanoTime() - startTime));
    }

    return extensionInformationOptional;
  }

  private Optional<LoadedExtensionInformation> withTemporaryApplication(BundleDescriptor pluginDescriptor,
                                                                        Map<String, Object> classLoaderModelLoaderAttributes,
                                                                        TemporaryApplicationFunction action) {
    String uuid = getUUID();
    String applicationName = uuid + "-extension-model-temp-app";
    File applicationFolder = new File(muleArtifactResourcesRegistry.getWorkingDirectory(), applicationName);
    try {
      createPomFile(pluginDescriptor, uuid, applicationFolder);

      MuleApplicationModel muleApplicationModel = new MuleApplicationModel.MuleApplicationModelBuilder()
          .setMinMuleVersion(getProductVersion())
          .setName(applicationName)
          .setRequiredProduct(MULE)
          .withBundleDescriptorLoader(new MuleArtifactLoaderDescriptor(MULE_LOADER_ID, emptyMap()))
          .withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(MULE_LOADER_ID,
                                                                                 classLoaderModelLoaderAttributes))
          .build();
      ApplicationDescriptor artifactDescriptor = muleArtifactResourcesRegistry.getApplicationDescriptorFactory()
          .createArtifact(applicationFolder, empty(), muleApplicationModel);

      ToolingApplicationClassLoaderBuilder builder =
          new ToolingApplicationClassLoaderBuilder(newTemporaryArtifactClassLoaderFactory(),
                                                   muleArtifactResourcesRegistry.getRegionPluginClassLoadersFactory());
      builder.setArtifactDescriptor(artifactDescriptor);
      builder.setParentClassLoader(muleArtifactResourcesRegistry.getContainerArtifactClassLoader());

      muleArtifactResourcesRegistry.getPluginDependenciesResolver()
          .resolve(emptySet(), new ArrayList<>(artifactDescriptor.getPlugins()), false)
          .stream()
          .forEach(builder::addArtifactPluginDescriptors);

      ToolingArtifactClassLoader toolingArtifactClassLoader = builder.build();

      try {
        ArtifactPluginDescriptor artifactPluginDescriptor = toolingArtifactClassLoader.getArtifactPluginClassLoaders().stream()
            .filter(artifactPluginClassLoader -> artifactPluginClassLoader.getArtifactDescriptor().getBundleDescriptor()
                .getGroupId()
                .equals(pluginDescriptor.getGroupId())
                && artifactPluginClassLoader.getArtifactDescriptor().getBundleDescriptor().getArtifactId()
                    .equals(pluginDescriptor.getArtifactId()))
            .findFirst().orElseThrow(() -> new IllegalStateException(format("Couldn't find plugin descriptor: %s",
                                                                            pluginDescriptor)))
            .getArtifactDescriptor();

        return action.call(artifactPluginDescriptor, toolingArtifactClassLoader, pluginDescriptor.getProperties());
      } catch (Exception e) {
        throw new ToolingException(e);
      } finally {
        if (toolingArtifactClassLoader != null) {
          toolingArtifactClassLoader.dispose();
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

  private void createPomFile(BundleDescriptor pluginDescriptor, String uuid, File applicationFolder) {
    Model model = new Model();
    model.setGroupId(uuid);
    model.setArtifactId(uuid);
    model.setVersion(getProductVersion());
    model.setPackaging(MULE_APPLICATION);
    model.setModelVersion(MAVEN_MODEL_VERSION);

    Dependency dependency = new Dependency();
    dependency.setGroupId(pluginDescriptor.getGroupId());
    dependency.setArtifactId(pluginDescriptor.getArtifactId());
    dependency.setVersion(pluginDescriptor.getVersion());
    dependency.setClassifier(MULE_PLUGIN_CLASSIFIER);
    dependency.setType(JAR);
    model.getDependencies().add(dependency);

    createDeployablePomFile(applicationFolder, model);
    createDeployablePomProperties(applicationFolder, model);
  }

  @FunctionalInterface
  interface TemporaryApplicationFunction {

    Optional<LoadedExtensionInformation> call(ArtifactPluginDescriptor artifactPluginDescriptor,
                                              ToolingArtifactClassLoader toolingArtifactClassLoader,
                                              Map<String, String> properties);
  }

  private DeployableArtifactClassLoaderFactory<ApplicationDescriptor> newTemporaryArtifactClassLoaderFactory() {
    return new DeployableArtifactClassLoaderFactory<ApplicationDescriptor>() {

      /**
       * {@inheritDoc}
       */
      @Override
      public ArtifactClassLoader create(String artifactId, ArtifactClassLoader parent,
                                        ApplicationDescriptor descriptor,
                                        List<ArtifactClassLoader> artifactPluginClassLoaders) {
        return new MuleDeployableArtifactClassLoader(artifactId, descriptor, descriptor.getClassLoaderModel().getUrls(),
                                                     parent.getClassLoader(),
                                                     parent.getClassLoaderLookupPolicy(), artifactPluginClassLoaders);
      }

    };
  }

  private Optional<LoadedExtensionInformation> loadExtensionData(ArtifactPluginDescriptor artifactPluginDescriptor,
                                                                 ToolingArtifactClassLoader toolingArtifactClassLoader,
                                                                 Map<String, String> properties) {
    try {
      MuleExtensionModelLoaderManager extensionModelLoaderRepository =
          new MuleExtensionModelLoaderManager(muleArtifactResourcesRegistry.getContainerArtifactClassLoader());
      extensionModelLoaderRepository.start();
      final Optional<LoadedExtensionInformation> loadedExtensionInformation =
          getLoadedExtensionInformation(artifactPluginDescriptor, toolingArtifactClassLoader.getArtifactPluginClassLoaders(), extensionModelLoaderRepository,
                                        properties);
      return loadedExtensionInformation;
    } catch (Exception e) {
      throw new ToolingException(e);
    } finally {
      if (toolingArtifactClassLoader != null) {
        toolingArtifactClassLoader.dispose();
      }
    }
  }

  private Optional<LoadedExtensionInformation> getLoadedExtensionInformation(ArtifactPluginDescriptor pluginDescriptor,
                                                                             List<ArtifactClassLoader> artifactPluginClassLoaders,
                                                                             MuleExtensionModelLoaderManager loaderRepository,
                                                                             Map<String, String> properties) {
    Set<Pair<ArtifactPluginDescriptor, ExtensionModel>> descriptorsWithExtensions =
        discoverPluginsExtensionModel(artifactPluginClassLoaders, loaderRepository, properties).stream()
            .map(pair -> withContextClassLoader(this.getClass().getClassLoader(), () -> {
              ExtensionModelJsonSerializer extensionModelJsonSerializer = new ExtensionModelJsonSerializer();
              Pair<ArtifactPluginDescriptor, ExtensionModel> result = new Pair<>(pair.getFirst(), extensionModelJsonSerializer
                  .deserialize(extensionModelJsonSerializer.serialize(pair.getSecond())));
              return result;
            }))
            .collect(toSet());
    Optional<ExtensionModel> foundExtension = getExtensionModel(pluginDescriptor, descriptorsWithExtensions);
    if (foundExtension.isPresent()) {
      return Optional
          .of(new LoadedExtensionInformation(foundExtension.get(), pluginDescriptor.getMinMuleVersion().toString()));
    } else {
      return empty();
    }
  }

  private Optional<ExtensionModel> getExtensionModel(ArtifactPluginDescriptor descriptor,
                                                     Set<Pair<ArtifactPluginDescriptor, ExtensionModel>> descriptorsWithExtensions) {
    if (descriptorsWithExtensions.isEmpty()) {
      return empty();
    }
    return descriptorsWithExtensions.stream().filter(e -> e.getFirst().equals(descriptor)).map(Pair::getSecond).findFirst();
  }

  private Set<Pair<ArtifactPluginDescriptor, ExtensionModel>> discoverPluginsExtensionModel(List<ArtifactClassLoader> artifactPluginClassLoaders,
                                                                                            MuleExtensionModelLoaderManager extensionModelLoaderRepository,
                                                                                            Map<String, String> properties) {
    List<Pair<ArtifactPluginDescriptor, ArtifactClassLoader>> artifacts = artifactPluginClassLoaders
        .stream()
        .map(a -> new Pair<>(effectiveModel(properties, a.getArtifactDescriptor()), a))
        .collect(toList());
    return extensionModelDiscoverer.discoverPluginsExtensionModels(extensionModelLoaderRepository, artifacts,
                                                                   copyOf(loadRuntimeExtensionModels()));
  }

  private ArtifactPluginDescriptor effectiveModel(Map<String, String> properties, ArtifactPluginDescriptor artifactDescriptor) {
    if (valueOf(properties.getOrDefault(DISABLE_COMPONENT_IGNORE, "false"))) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info(format("Loading effective model for '%s'", artifactDescriptor.getBundleDescriptor()));
      }
      artifactDescriptor.getExtensionModelDescriptorProperty()
          .ifPresent(extensionModelDescriptorProperty -> extensionModelDescriptorProperty
              .addAttributes(ImmutableMap.of(DISABLE_COMPONENT_IGNORE, TRUE)));
    }
    return artifactDescriptor;
  }
  

  @Override
  public Optional<LoadedExtensionInformation> loadExtensionData(ArtifactClassLoader artifactClassLoader,
                                                                List<ArtifactClassLoader> artifactPluginClassLoaders) {
    List<ArtifactPluginDescriptor> artifactPluginDescriptors = new ArrayList<>();
    artifactPluginDescriptors.add(artifactClassLoader.getArtifactDescriptor());
    List<ArtifactClassLoader> resolvedArtifactPluginClassLoaders = muleArtifactResourcesRegistry.getPluginDependenciesResolver()
        .resolve(emptySet(), artifactPluginDescriptors, false)
        .stream()
        .map(dependentArtifactPluginDescriptor -> artifactPluginClassLoaders.stream()
            .filter(classloader -> classloader.getArtifactDescriptor().getBundleDescriptor()
                .equals(dependentArtifactPluginDescriptor.getBundleDescriptor()))
            .findAny()
            .orElseThrow(() -> new MuleRuntimeException(createStaticMessage("Couldn't find a matching class loader provided by the context for plugin descriptor: "
                + dependentArtifactPluginDescriptor.getBundleDescriptor()))))
        .collect(toList());

    return loadExtensionData(artifactClassLoader.getArtifactDescriptor(), resolvedArtifactPluginClassLoaders, emptyMap());
  }
  
  private Optional<LoadedExtensionInformation> loadExtensionData(ArtifactPluginDescriptor artifactPluginDescriptor,
                                                                 List<ArtifactClassLoader> artifactPluginClassLoaders,
                                                                 Map<String, String> properties) {
    try {
      MuleExtensionModelLoaderManager extensionModelLoaderRepository =
          new MuleExtensionModelLoaderManager(muleArtifactResourcesRegistry.getContainerArtifactClassLoader());
      extensionModelLoaderRepository.start();
      final Optional<LoadedExtensionInformation> loadedExtensionInformation =
          getLoadedExtensionInformation(artifactPluginDescriptor, artifactPluginClassLoaders,
                                        extensionModelLoaderRepository,
                                        properties);
      return loadedExtensionInformation;
    } catch (Exception e) {
      throw new ToolingException(e);
    }
  }

}

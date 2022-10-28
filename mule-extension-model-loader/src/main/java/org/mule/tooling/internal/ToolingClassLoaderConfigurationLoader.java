package org.mule.tooling.internal;

import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.mule.runtime.deployment.model.api.artifact.ArtifactDescriptorConstants.MULE_LOADER_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.core.api.config.bootstrap.ArtifactType;
import org.mule.runtime.module.artifact.api.descriptor.ClassLoaderConfiguration;
import org.mule.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
import org.mule.runtime.module.artifact.api.descriptor.InvalidDescriptorLoaderException;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

/**
 * Mimic for {@link org.mule.runtime.module.deployment.impl.internal.artifact.MavenClassLoaderConfigurationLoader} that allows to use
 * injected instances of deployable, plugin and lib class loader model loaders.
 */
public class ToolingClassLoaderConfigurationLoader implements ClassLoaderConfigurationLoader {

  private static final Logger LOGGER = getLogger(ToolingClassLoaderConfigurationLoader.class);

  private List<ClassLoaderConfigurationLoader> classLoaderModelLoaders;

  public ToolingClassLoaderConfigurationLoader(List<ClassLoaderConfigurationLoader> classLoaderModelLoaders) {
    this.classLoaderModelLoaders = classLoaderModelLoaders;
  }

  @Override
  public String getId() {
    return MULE_LOADER_ID;
  }

  @Override
  public ClassLoaderConfiguration load(File artifactFile, Map<String, Object> attributes, ArtifactType artifactType)
      throws InvalidDescriptorLoaderException {
    long startTime = nanoTime();
    for (ClassLoaderConfigurationLoader classLoaderModelLoader : classLoaderModelLoaders) {
      if (classLoaderModelLoader.supportsArtifactType(artifactType)) {
        ClassLoaderConfiguration classLoaderModel = classLoaderModelLoader.load(artifactFile, attributes, artifactType);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("ClassLoaderConfiguration for {} loaded in {}ms", artifactFile.getName(),
                       NANOSECONDS.toMillis(nanoTime() - startTime));
        }
        return classLoaderModel;
      }
    }
    throw new IllegalStateException(format("Artifact type %s not supported", artifactType));
  }

  @Override
  public boolean supportsArtifactType(ArtifactType artifactType) {
    return classLoaderModelLoaders.stream()
        .filter(classLoaderModelLoader -> classLoaderModelLoader.supportsArtifactType(artifactType)).findFirst().isPresent();
  }
}

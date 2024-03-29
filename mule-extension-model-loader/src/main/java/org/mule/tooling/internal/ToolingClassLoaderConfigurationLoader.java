/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.internal;

import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptorConstants.MULE_LOADER_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.core.api.config.bootstrap.ArtifactType;
import org.mule.runtime.module.artifact.api.descriptor.ClassLoaderConfiguration;
import org.mule.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
import org.mule.runtime.module.artifact.api.descriptor.InvalidDescriptorLoaderException;
import org.mule.runtime.module.deployment.impl.internal.artifact.MavenClassLoaderConfigurationLoader;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

/**
 * Mimic for {@link MavenClassLoaderConfigurationLoader} that allows to use injected instances of deployable, plugin
 * and lib class loader configuration loaders.
 */
public class ToolingClassLoaderConfigurationLoader implements ClassLoaderConfigurationLoader {

  private static final Logger LOGGER = getLogger(ToolingClassLoaderConfigurationLoader.class);

  private final List<ClassLoaderConfigurationLoader> classLoaderConfigurationLoaders;

  public ToolingClassLoaderConfigurationLoader(List<ClassLoaderConfigurationLoader> classLoaderConfigurationLoaders) {
    this.classLoaderConfigurationLoaders = classLoaderConfigurationLoaders;
  }

  @Override
  public String getId() {
    return MULE_LOADER_ID;
  }

  @Override
  public ClassLoaderConfiguration load(File artifactFile, Map<String, Object> attributes, ArtifactType artifactType)
      throws InvalidDescriptorLoaderException {
    long startTime = nanoTime();
    for (ClassLoaderConfigurationLoader classLoaderConfigurationLoader : classLoaderConfigurationLoaders) {
      if (classLoaderConfigurationLoader.supportsArtifactType(artifactType)) {
        ClassLoaderConfiguration classLoaderConfiguration =
            classLoaderConfigurationLoader.load(artifactFile, attributes, artifactType);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("ClassLoaderConfiguration for {} loaded in {}ms", artifactFile.getName(),
                       NANOSECONDS.toMillis(nanoTime() - startTime));
        }
        return classLoaderConfiguration;
      }
    }
    throw new IllegalStateException(format("Artifact type %s not supported", artifactType));
  }

  @Override
  public boolean supportsArtifactType(ArtifactType artifactType) {
    return classLoaderConfigurationLoaders.stream()
        .anyMatch(classLoaderConfigurationLoader -> classLoaderConfigurationLoader.supportsArtifactType(artifactType));
  }
}

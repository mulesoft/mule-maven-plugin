package org.mule.tooling.internal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mule.runtime.core.api.config.bootstrap.ArtifactType;
import org.mule.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
import org.mule.runtime.module.artifact.api.descriptor.InvalidDescriptorLoaderException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolingClassLoaderConfigurationLoaderTest {

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void loadTest(boolean traceEnabled) throws InvalidDescriptorLoaderException {
    System.setProperty("logging.level.org.mule.tooling.internal.ToolingClassLoaderConfigurationLoader",
                       traceEnabled ? "TRACE" : "DEBUG");
    System.setProperty("org.apache.logging.log4j.simplelog.StatusLogger.level", traceEnabled ? "TRACE" : "DEBUG");
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", traceEnabled ? "TRACE" : "DEBUG");

    List<ClassLoaderConfigurationLoader> classLoaderConfigurationLoaders = new ArrayList<>();
    ToolingClassLoaderConfigurationLoader loader = new ToolingClassLoaderConfigurationLoader(classLoaderConfigurationLoaders);

    assertThatThrownBy(() -> loader.load(mock(File.class), Collections.emptyMap(), ArtifactType.DOMAIN))
        .isInstanceOf(IllegalStateException.class).hasMessageContaining("Artifact type").hasMessageContaining("not supported");
    //
    ClassLoaderConfigurationLoader mockLoader = Mockito.mock(ClassLoaderConfigurationLoader.class);
    when(mockLoader.supportsArtifactType(any(ArtifactType.class))).thenReturn(true);

    classLoaderConfigurationLoaders.add(mockLoader);
    loader.load(mock(File.class), Collections.emptyMap(), ArtifactType.DOMAIN);

  }
}

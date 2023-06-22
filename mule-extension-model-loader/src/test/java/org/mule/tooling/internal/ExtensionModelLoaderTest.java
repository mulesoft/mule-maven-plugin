/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.container.internal.ModuleDiscoverer;
import org.mule.tooling.api.ExtensionModelLoader;
import org.mule.tooling.api.ExtensionModelLoaderFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ExtensionModelLoaderTest extends MavenClientTest {

  @Test
  void loadExtensionModelFromJar2(@TempDir Path tempDir) throws IOException {
    final Path temp = Files.createDirectories(tempDir.resolve("dummy/"));
    final File m2Repo = getM2Repo(getM2Home());
    final MavenClient client = getMavenClientInstance(getMavenConfiguration(m2Repo, getUserSettings(m2Repo), getSettingsSecurity(m2Repo)));
    final ExtensionModelLoader extensionModelLoader = ExtensionModelLoaderFactory.createLoader(client, temp, ModuleDiscoverer.class.getClassLoader(), "4.4.0");
    final BundleDescriptor bundleDescriptor = new BundleDescriptor.Builder()
      .setGroupId("org.mule.connectors")
      .setArtifactId("mule-http-connector")
      .setClassifier("mule-plugin")
      .setVersion("1.5.25").build();
    final PluginResources http = extensionModelLoader.load(bundleDescriptor);

    assertThat(extensionModelLoader.getRuntimeExtensionModels().size())
      .as("Loaded a different amount of runtime extension models than expected")
      .isEqualTo(10);

    assertThat(extensionModelLoader.getRuntimeExtensionModels().stream().map(ExtensionModel::getName).collect(Collectors.toList()))
      .as("No all extensions models has been loaded")
      .containsAll(Arrays.asList("ee", "mule", "bti", "module", "api-gateway", "batch", "tls", "http-policy", "tracking", "kryo"));

    assertThat(http.getExtensionModels().size())
      .as("Loaded a different amount of plugin extension models than expected")
      .isEqualTo(7);

    assertThat(http.getExtensionModels().stream().map(ExtensionModel::getName).collect(Collectors.toList()))
      .as("No all extensions models has been loaded")
      .containsAll(Arrays.asList("ee", "mule", "Sockets", "module", "api-gateway", "tls", "HTTP"));
  }
}

package org.mule.tooling.internal;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.api.model.MavenConfiguration;
import org.mule.maven.client.api.model.RemoteRepository;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.container.internal.ModuleDiscoverer;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor;

import org.mule.tooling.api.ExtensionModelLoader;
import org.mule.tooling.api.ExtensionModelLoaderFactory;

public class ExtensionModelLoaderTest extends MavenClientTest {



  @Rule
  public TemporaryFolder temporaryFolder = TemporaryFolder.builder().build();

  @Test
  public void loadExtensionModelFromJar2() throws IOException {
    Path temp = temporaryFolder.newFolder("dummy").toPath();
    final File m2Repo = getM2Repo(getM2Home());
    MavenClient client = getMavenClientInstance(
                                                getMavenConfiguration(m2Repo, Optional.ofNullable(getUserSettings(m2Repo)),
                                                                      Optional.ofNullable(getSettingsSecurity(m2Repo))));

    ExtensionModelLoader extensionModelLoader =
        ExtensionModelLoaderFactory.createLoader(client, temp, ModuleDiscoverer.class.getClassLoader(), "4.4.0");

    PluginResources http = extensionModelLoader.load(
                                                                                new BundleDescriptor.Builder()
                                                                                    .setGroupId("org.mule.connectors")
                                                                                    .setArtifactId("mule-http-connector")
                                                                                    .setClassifier("mule-plugin")
                                                                                    .setVersion("1.5.25").build());
    assertEquals("Loaded a different amount of runtime extension models than expected", 10,
            extensionModelLoader.getRuntimeExtensionModels().size());
    assertEquals("Loaded a different amount of plugin extension models than expected",2,http.getExtensionModels().size());

  }

}

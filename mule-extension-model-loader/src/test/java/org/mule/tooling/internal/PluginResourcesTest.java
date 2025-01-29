package org.mule.tooling.internal;

import org.junit.jupiter.api.Test;
import org.mule.runtime.api.meta.model.ExtensionModel;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PluginResourcesTest {

  private final Set<ExtensionModel> extensionModels = new HashSet<>();
  private final List<URL> exportedResources = new ArrayList<>();
  private final PluginResources pluginResources = new PluginResources(extensionModels, exportedResources);

  @Test
  void getExportedResourcesTest() {
    assertThat(pluginResources.getExportedResources()).containsExactlyElementsOf(exportedResources);
  }
}

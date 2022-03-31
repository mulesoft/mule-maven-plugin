package org.mule.tooling.internal;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor;

public class PluginResources {

  private Set<Pair<ArtifactPluginDescriptor, ExtensionModel>> extensionModels;
  private List<URL> exportedResources;

  public PluginResources(Set<Pair<ArtifactPluginDescriptor, ExtensionModel>> extensionModels, List<URL> exportedResources) {
    super();
    this.extensionModels = extensionModels;
    this.exportedResources = exportedResources;
  }

  public List<URL> getExportedResources() {
    return exportedResources;
  }


  public Set<Pair<ArtifactPluginDescriptor, ExtensionModel>> getExtensionModels() {
    return extensionModels;
  }
}

package org.mule.tooling.internal;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.mule.runtime.api.meta.model.ExtensionModel;

public class PluginResources {

  private Set<ExtensionModel> extensionModels;
  private List<URL> exportedResources;

  public PluginResources(Set<ExtensionModel> extensionModels, List<URL> exportedResources) {
    super();
    this.extensionModels = extensionModels;
    this.exportedResources = exportedResources;
  }

  public List<URL> getExportedResources() {
    return exportedResources;
  }


  public Set<ExtensionModel> getExtensionModels() {
    return extensionModels;
  }
}

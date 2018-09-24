package org.mule.tools.api.classloader.model;


import java.util.List;

public class AppClassLoaderModel extends ClassLoaderModel {

  private List<Plugin> additionalPluginDependencies;

  public AppClassLoaderModel(String version, ArtifactCoordinates artifactCoordinates) {
    super(version, artifactCoordinates);
  }

  public List<Plugin> getAdditionalPluginDependencies() {
    return additionalPluginDependencies;
  }

  public void setAdditionalPluginDependencies(List<Plugin> additionalPluginDependencies) {
    this.additionalPluginDependencies = additionalPluginDependencies;
  }
}

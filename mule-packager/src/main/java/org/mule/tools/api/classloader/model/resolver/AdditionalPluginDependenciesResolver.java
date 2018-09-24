package org.mule.tools.api.classloader.model.resolver;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.Plugin;
import org.mule.tools.api.classloader.model.util.ArtifactUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luciano.raineri on 9/24/18.
 */
public class AdditionalPluginDependenciesResolver extends ClassloaderModelResolver{

  private List<Plugin> pluginsWithAdditionalDependencies;

  public AdditionalPluginDependenciesResolver(AetherMavenClient muleMavenPluginClient, List<Plugin> additionalPluginDependencies) {
    super(muleMavenPluginClient, null);
    this.pluginsWithAdditionalDependencies = additionalPluginDependencies;
  }

  @Override
  protected List<BundleDependency> resolveConflicts(List<BundleDependency> newDependencies, List<BundleDependency> alreadyResolved) {
    List<BundleDependency> dependencies = new ArrayList<>();
    dependencies.addAll(alreadyResolved);
    dependencies.addAll(newDependencies);
    return dependencies;
  }

  @Override
  public Map<BundleDependency, List<BundleDependency>> resolveDependencies(List<BundleDependency> mulePlugins) {
    Map<BundleDependency, List<BundleDependency>> additionalDeps = new LinkedHashMap<>();
    for (BundleDependency muleDependency : mulePlugins) {
      List<BundleDependency> mulePluginDependencies = new ArrayList<>();
      getAdditionalDependencies(muleDependency.getDescriptor()).forEach(
              additionalDepDescriptor -> {
                mulePluginDependencies.add(muleMavenPluginClient
                                                   .resolveBundleDescriptor(additionalDepDescriptor));
                mulePluginDependencies.addAll(muleMavenPluginClient
                                                      .resolveBundleDescriptorDependencies(false, false,
                                                                                           additionalDepDescriptor));
              });
    }
    return additionalDeps;
  }

  private List<BundleDescriptor> getAdditionalDependencies(BundleDescriptor plugin) {
    return pluginsWithAdditionalDependencies
            .stream()
            .filter(
                    pluginWithAdditionalDependencies -> pluginWithAdditionalDependencies.getArtifactId()
                                                                .equals(plugin.getArtifactId())
                                                        && pluginWithAdditionalDependencies.getGroupId().equals(plugin.getGroupId()))
            .findFirst()
            .map((pluginWithAdditionalDependencies) -> pluginWithAdditionalDependencies.getDependencies().stream()
                    .map(dep -> ArtifactUtils.toBundleDescriptor(dep)).collect(toList()))
            .orElse(emptyList());
  }

}

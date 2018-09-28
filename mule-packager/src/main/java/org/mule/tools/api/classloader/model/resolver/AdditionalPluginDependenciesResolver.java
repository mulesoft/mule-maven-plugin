/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model.resolver;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toBundleDescriptor;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;


/**
 * Resolves additional plugin libraries for all plugins declared.
 *
 * @since 3.2.0
 */
public class AdditionalPluginDependenciesResolver {

  private AetherMavenClient aetherMavenClient;
  private List<Plugin> pluginsWithAdditionalDependencies;

  public AdditionalPluginDependenciesResolver(AetherMavenClient muleMavenPluginClient,
                                              List<Plugin> additionalPluginDependencies) {
    this.aetherMavenClient = muleMavenPluginClient;
    this.pluginsWithAdditionalDependencies = additionalPluginDependencies;
  }

  public Map<BundleDependency, List<BundleDependency>> resolveDependencies(List<BundleDependency> mulePlugins,
                                                                           Collection<ClassLoaderModel> mulePluginsClassLoaderModels) {
    Map<BundleDependency, List<BundleDependency>> pluginsWithAdditionalDeps = new LinkedHashMap<>();
    for (Plugin pluginWithAdditionalDependencies : pluginsWithAdditionalDependencies) {
      BundleDependency pluginBundleDependency = getPluginBundleDependency(pluginWithAdditionalDependencies, mulePlugins);
      ClassLoaderModel pluginClassLoaderModel =
          getPluginClassLoaderModel(pluginWithAdditionalDependencies, mulePluginsClassLoaderModels);
      List<BundleDependency> additionalDependencies = new ArrayList<>();
      pluginWithAdditionalDependencies.getAdditionalDependencies().stream()
          .filter(additionalDep -> !isPresentInClassLoaderModel(pluginClassLoaderModel, additionalDep))
          .forEach(
                   dep -> {
                     additionalDependencies.add(aetherMavenClient
                         .resolveBundleDescriptor(toBundleDescriptor(dep)));
                     additionalDependencies.addAll(aetherMavenClient
                         .resolveBundleDescriptorDependencies(false,
                                                              false,
                                                              toBundleDescriptor(dep)));
                   });
      if (!additionalDependencies.isEmpty()) {
        pluginsWithAdditionalDeps.put(pluginBundleDependency,
                                      additionalDependencies);
      }
    }
    return pluginsWithAdditionalDeps;
  }

  private BundleDependency getPluginBundleDependency(Plugin plugin, List<BundleDependency> mulePlugins) {
    return mulePlugins.stream().filter(mulePlugin -> mulePlugin.getDescriptor().getArtifactId().equals(plugin.getArtifactId())
        && mulePlugin.getDescriptor().getGroupId().equals(plugin.getGroupId()))
        .findFirst()
        .orElseThrow(() -> new MuleRuntimeException(createStaticMessage("Declared additional dependencies for a plugin not present: "
            + plugin)));
  }

  private ClassLoaderModel getPluginClassLoaderModel(Plugin plugin, Collection<ClassLoaderModel> mulePluginsClassLoaderModels) {
    return mulePluginsClassLoaderModels.stream().filter(
                                                        pluginClassLoaderModel -> pluginClassLoaderModel.getArtifactCoordinates()
                                                            .getGroupId().equals(plugin.getGroupId())
                                                            && pluginClassLoaderModel.getArtifactCoordinates().getArtifactId()
                                                                .equals(plugin.getArtifactId()))
        .findFirst()
        .orElseThrow(() -> new MuleRuntimeException(createStaticMessage("Could not find ClassLoaderModel resolved for plugin: "
            + plugin)));
  }

  private boolean areSameArtifact(Dependency dependency, Artifact artifact) {
    return dependency.getArtifactId().equals(artifact.getArtifactCoordinates().getArtifactId())
        && dependency.getGroupId().equals(artifact.getArtifactCoordinates().getGroupId())
        && dependency.getVersion().equals(artifact.getArtifactCoordinates().getVersion());
  }

  private boolean isPresentInClassLoaderModel(ClassLoaderModel classLoaderModel, Dependency dep) {
    return classLoaderModel.getDependencies().stream().anyMatch(artifactDependency -> areSameArtifact(dep, artifactDependency));
  }

}

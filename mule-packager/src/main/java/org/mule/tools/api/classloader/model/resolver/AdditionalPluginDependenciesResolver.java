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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toBundleDescriptor;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.util.ArtifactUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


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

  public Map<BundleDependency, List<BundleDependency>> resolveDependencies(List<BundleDependency> mulePlugins) {
    Map<BundleDependency, List<BundleDependency>> pluginsWithAdditionalDeps = new LinkedHashMap<>();
    for (Plugin pluginWithAdditionalDependencies : pluginsWithAdditionalDependencies) {
      List<BundleDependency> additionalDependencies = new ArrayList<>();
      pluginWithAdditionalDependencies.getAdditionalDependencies().forEach(
                                                                           dep -> {
                                                                             additionalDependencies.add(aetherMavenClient
                                                                                 .resolveBundleDescriptor(toBundleDescriptor(dep)));
                                                                             additionalDependencies.addAll(aetherMavenClient
                                                                                 .resolveBundleDescriptorDependencies(false,
                                                                                                                      false,
                                                                                                                      toBundleDescriptor(dep)));
                                                                           });
      pluginsWithAdditionalDeps.put(getPluginBundleDependency(pluginWithAdditionalDependencies, mulePlugins),
                                    additionalDependencies);
    }
    return pluginsWithAdditionalDeps;
  }

  private BundleDependency getPluginBundleDependency(Plugin plugin, List<BundleDependency> mulePlugins) {
    return mulePlugins.stream().filter(mulePlugin -> mulePlugin.getDescriptor().getArtifactId().equals(plugin.getArtifactId())
        && mulePlugin.getDescriptor().getGroupId().equals(plugin.getGroupId()))
        .findFirst().orElseThrow(RuntimeException::new);
  }

}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.classloader.model.util;

import static java.util.stream.Collectors.toList;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toApplicationModelArtifacts;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.updatePackagesResources;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.tools.api.classloader.model.Plugin;

import java.util.List;
import java.util.Map;

/**
 * Utils for {@link Plugin} construction.
 *
 * @since 3.2.0
 */
public class PluginUtils {

  public static List<Plugin> toPluginDependencies(Map<BundleDependency, List<BundleDependency>> pluginsAndDependencies) {
    return pluginsAndDependencies.entrySet().stream().map(
                                                          (pluginEntry) -> {
                                                            Plugin plugin = new Plugin();
                                                            plugin.setArtifactId(pluginEntry.getKey().getDescriptor()
                                                                .getArtifactId());
                                                            plugin.setGroupId(pluginEntry.getKey().getDescriptor().getGroupId());
                                                            plugin.setAdditionalDependencies(
                                                                                             updatePackagesResources(toApplicationModelArtifacts(pluginEntry
                                                                                                 .getValue())));
                                                            return plugin;
                                                          })
        .collect(toList());


  }


}

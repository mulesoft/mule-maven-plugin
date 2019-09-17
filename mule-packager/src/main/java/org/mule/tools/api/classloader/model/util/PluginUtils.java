/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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

package org.mule.tools.api.classloader.model.util;

import static java.util.stream.Collectors.toList;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.tools.api.classloader.model.Plugin;

import java.util.List;
import java.util.Map;

/**
 * Created by luciano.raineri on 9/24/18.
 */
public class PluginUtils {

  public static List<Plugin> toPluginList(Map<BundleDependency, List<BundleDependency>> pluginsAndDependencies) {
    return pluginsAndDependencies.entrySet().stream().map(
            (pluginBundleDependency, dependencies) -> {
              Plugin plugin = new Plugin();
              plugin.
            }
    ).collect(toList());


  }


}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.util;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The goal of this class is to check for if a list of Dependencies are compatible amongst them self
 * Compatibility is defined by semantic versioning
 */
public class MulePluginsCompatibilityValidator {

    /**
     * Validates a list of dependencies to check for incompatibilities
     *
     * @param mulePlugins
     * @throws MojoExecutionException if the list of mule plugins contains incompatibilities
     */
    public void validate(List<Dependency> mulePlugins) throws MojoExecutionException {
        for (Map.Entry<String, List<Dependency>> entry : buildDependencyMap(mulePlugins).entrySet()) {
            if (entry.getValue().size() > 1) {

                if (!areMulePluginVersionCompatible(entry.getValue())) {
                    StringBuilder message = new StringBuilder()
                        .append("There are incompatible versions of the same mule plugin in the application dependency graph.")
                        .append("This application can not be package as it will fail to deploy.")
                        .append("Offending mule plugin: ").append(entry.getKey())
                        .append("Versions: ");
                    entry.getValue().forEach(d -> message.append(d.getVersion()).append(","));

                    throw new MojoExecutionException(message.toString());
                }
            }
        }
    }

    private Map<String, List<Dependency>> buildDependencyMap(List<Dependency> dependencyList) {
        Map<String, List<Dependency>> dependencyMap = new HashMap<>();

        for (Dependency plugin : dependencyList) {
            String key = plugin.getGroupId() + ":" + plugin.getArtifactId();

            if (dependencyMap.get(key) == null) {
                dependencyMap.put(key, Arrays.asList(plugin));
            } else {
                dependencyMap.get(key).add(plugin);
            }
        }

        return dependencyMap;
    }

    private boolean areMulePluginVersionCompatible(List<Dependency> dependencies) {
        Set<String> majors = dependencies.stream()
            .map(d -> d.getVersion())
            .map(v -> v.substring(0, v.indexOf(".")))
            .collect(Collectors.toSet());

        if (majors.size() > 1) {
            return false;
        }
        return true;

    }

} 

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.dependency;

import org.apache.maven.model.Dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The goal of this class is to select always the newer version of a plugin
 */
public class ApplicationDependencySelector {

    public List<Dependency> select(List<Dependency> mulePlugins) {
        List<Dependency> selectedDependencies = new ArrayList<>();

        Map<String, List<Dependency>> dependencyMap = new DependencyMapBuilder().build(mulePlugins);
        selectedDependencies.addAll(
            dependencyMap.entrySet().stream().map(entry -> getNewerPluginFile(entry.getValue())).collect(Collectors.toList()));

        return selectedDependencies;
    }

    /**
     * Given a list of dependencies all representing the same artifact, different versions it will select the newer version and return the file of that one.
     *
     * @param dependencyVersions list of dependencies, all the same artifact different versions
     * @return returns the file of th newer version of the dependencies list
     */
    private Dependency getNewerPluginFile(List<Dependency> dependencyVersions) {
        if (dependencyVersions.size() == 1) {
            return dependencyVersions.get(0);
        }

        Dependency newerDependency = dependencyVersions.get(0);
        for (Dependency dependency : dependencyVersions) {
            if (dependency.getVersion().compareTo(newerDependency.getVersion()) > 1) {
                newerDependency = dependency;
            }
        }
        return newerDependency;
    }

} 

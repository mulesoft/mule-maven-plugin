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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyMapBuilder {

    public DependencyMapBuilder() {

    }

    public Map<String, List<Dependency>> build(List<Dependency> dependencyList) {
        Map<String, List<Dependency>> dependencyMap = new HashMap<>();

        for (Dependency plugin : dependencyList) {
            String key = plugin.getGroupId() + ":" + plugin.getArtifactId();

            dependencyMap.computeIfAbsent(key, k -> new ArrayList<>());
            dependencyMap.get(key).add(plugin);
        }

        return dependencyMap;
    }
}

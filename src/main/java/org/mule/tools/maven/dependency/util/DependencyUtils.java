/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.dependency.util;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.tools.maven.dependency.model.ArtifactCoordinates;
import org.mule.tools.maven.dependency.model.Dependency;

import java.util.*;
import java.util.stream.Collectors;

import static org.mule.maven.client.internal.AetherMavenClient.MULE_PLUGIN_CLASSIFIER;

public class DependencyUtils {

  public static ArtifactCoordinates toArtifactCoordinates(BundleDescriptor bundleDescriptor) {
    ArtifactCoordinates artifactCoordinates =
        new ArtifactCoordinates(bundleDescriptor.getGroupId(), bundleDescriptor.getArtifactId(), bundleDescriptor.getVersion(),
                                bundleDescriptor.getType(), bundleDescriptor.getClassifier().orElse(null));
    return artifactCoordinates;
  }

  public static Dependency toDependency(BundleDependency bundleDependency) {
    ArtifactCoordinates artifactCoordinates = toArtifactCoordinates(bundleDependency.getDescriptor());

    Dependency dependency = new Dependency(artifactCoordinates, bundleDependency.getBundleUri());

    return dependency;
  }

  public static SortedSet<Dependency> toDependencies(List<BundleDependency> dependencies) {
    return dependencies.stream().map(DependencyUtils::toDependency).collect(Collectors.toCollection(TreeSet::new));
  }

  public static boolean isValidMulePlugin(Dependency dependency) {
    ArtifactCoordinates pluginCoordinates = dependency.getArtifactCoordinates();
    Optional<String> pluginClassifier = Optional.ofNullable(pluginCoordinates.getClassifier());
    return pluginClassifier.isPresent() && MULE_PLUGIN_CLASSIFIER.equals(pluginClassifier.get());
  }
}

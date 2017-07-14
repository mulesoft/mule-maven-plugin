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

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.mule.maven.client.internal.AetherMavenClient.MULE_PLUGIN_CLASSIFIER;

public class ArtifactUtils {

  public static ArtifactCoordinates toArtifactCoordinates(BundleDescriptor bundleDescriptor) {
    ArtifactCoordinates artifactCoordinates =
        new ArtifactCoordinates(bundleDescriptor.getGroupId(), bundleDescriptor.getArtifactId(),
                                bundleDescriptor.getBaseVersion(),
                                bundleDescriptor.getType(), bundleDescriptor.getClassifier().orElse(null));
    return artifactCoordinates;
  }

  public static Artifact toArtifact(BundleDependency bundleDependency) {
    ArtifactCoordinates artifactCoordinates = toArtifactCoordinates(bundleDependency.getDescriptor());

    Artifact artifact = new Artifact(artifactCoordinates, bundleDependency.getBundleUri());

    return artifact;
  }

  public static List<Artifact> toArtifacts(List<BundleDependency> dependencies) {
    return dependencies.stream().map(ArtifactUtils::toArtifact).collect(Collectors.toList());
  }

  public static boolean isValidMulePlugin(Artifact artifact) {
    ArtifactCoordinates pluginCoordinates = artifact.getArtifactCoordinates();
    Optional<String> pluginClassifier = Optional.ofNullable(pluginCoordinates.getClassifier());
    return pluginClassifier.isPresent() && MULE_PLUGIN_CLASSIFIER.equals(pluginClassifier.get());
  }

  public static org.apache.maven.artifact.Artifact toArtifact(Artifact dependencyArtifact) {
    ArtifactCoordinates artifactCoordinates = dependencyArtifact.getArtifactCoordinates();
    org.apache.maven.artifact.Artifact artifact =
        new DefaultArtifact(artifactCoordinates.getGroupId(), artifactCoordinates.getArtifactId(),
                            artifactCoordinates.getVersion(), null, artifactCoordinates.getType(),
                            artifactCoordinates.getClassifier(),
                            new DefaultArtifactHandler(artifactCoordinates.getType()));
    artifact.setFile(new File(dependencyArtifact.getUri()));
    return artifact;
  }
}

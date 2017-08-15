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
import org.apache.maven.model.Dependency;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.mule.maven.client.internal.AetherMavenClient.MULE_PLUGIN_CLASSIFIER;

/**
 * ArtifactUtils presents helper methods to convert artifact related classes and recognize mule plugin artifacts.
 *
 */
public class ArtifactUtils {

  /**
   * Convert a {@link org.mule.maven.client.api.model.BundleDescriptor} instance to {@link ArtifactCoordinates}.
   *
   * @param bundleDescriptor the bundle descriptor to be converted.
   * @return the corresponding artifact coordinates with normalized version.
   */
  public static ArtifactCoordinates toArtifactCoordinates(BundleDescriptor bundleDescriptor) {
    ArtifactCoordinates artifactCoordinates =
        new ArtifactCoordinates(bundleDescriptor.getGroupId(), bundleDescriptor.getArtifactId(),
                                bundleDescriptor.getBaseVersion(),
                                bundleDescriptor.getType(), bundleDescriptor.getClassifier().orElse(null));
    return artifactCoordinates;
  }

  /**
   * Convert a {@link org.mule.maven.client.api.model.BundleDependency} instance to {@link Artifact}.
   *
   * @param bundleDependency the bundle dependency to be converted.
   * @return the corresponding artifact with normalized version.
   */
  public static Artifact toArtifact(BundleDependency bundleDependency) {
    ArtifactCoordinates artifactCoordinates = toArtifactCoordinates(bundleDependency.getDescriptor());
    return new Artifact(artifactCoordinates, bundleDependency.getBundleUri());
  }

  /**
   * Converts a {@link List<org.mule.maven.client.api.model.BundleDependency>} to a {@link List<Artifact>}.
   *
   * @param dependencies the bundle dependency list to be converted.
   * @return the corresponding artifact list, each one with normalized version.
   */
  public static List<Artifact> toArtifacts(Collection<BundleDependency> dependencies) {
    return dependencies.stream().map(ArtifactUtils::toArtifact).collect(Collectors.toList());
  }

  /**
   * Checks if a {@link Artifact} instance represents a mule-plugin.
   *
   * @param artifact the artifact to be checked.
   * @return true if the artifact is a mule-plugin, false otherwise.
   */
  public static boolean isValidMulePlugin(Artifact artifact) {
    ArtifactCoordinates pluginCoordinates = artifact.getArtifactCoordinates();
    Optional<String> pluginClassifier = Optional.ofNullable(pluginCoordinates.getClassifier());
    return pluginClassifier.isPresent() && MULE_PLUGIN_CLASSIFIER.equals(pluginClassifier.get());
  }

  /**
   * Checks if a {@link org.apache.maven.artifact.Artifact} instance represents a mule-plugin.
   *
   * @param artifact the artifact to be checked.
   * @return true if the artifact is a mule-plugin, false otherwise.
   */
  public static boolean isValidMulePlugin(org.apache.maven.artifact.Artifact artifact) {
    Optional<String> pluginClassifier = Optional.ofNullable(artifact.getClassifier());
    return pluginClassifier.isPresent() && MULE_PLUGIN_CLASSIFIER.equals(pluginClassifier.get());
  }

  /**
   * Converts a {@link Artifact} instance to a {@link org.apache.maven.artifact.Artifact} instance.
   *
   * @param dependencyArtifact the artifact to be converted.
   * @return the corresponding {@link org.apache.maven.artifact.Artifact} instance.
   */
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

  /**
   * Converts a {@link org.apache.maven.artifact.Artifact} instance to a {@link ArtifactCoordinates} instance.
   *
   * @param artifact the artifact to be converted.
   * @return the corresponding {@link ArtifactCoordinates} instance.
   */
  public static ArtifactCoordinates toArtifactCoordinates(org.apache.maven.artifact.Artifact artifact) {
    ArtifactCoordinates artifactCoordinates =
        new ArtifactCoordinates(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion());
    artifactCoordinates.setType(artifact.getType());
    artifactCoordinates.setClassifier(artifact.getClassifier());
    return artifactCoordinates;
  }

  public static Dependency toDependency(ArtifactCoordinates artifactCoordinates) {
    Dependency dependency = new Dependency();
    dependency.setGroupId(artifactCoordinates.getGroupId());
    dependency.setArtifactId(artifactCoordinates.getArtifactId());
    dependency.setVersion(artifactCoordinates.getVersion());
    dependency.setType(artifactCoordinates.getType());
    dependency.setClassifier(artifactCoordinates.getClassifier());
    dependency.setScope(artifactCoordinates.getScope());
    return dependency;
  }

  public static ArtifactCoordinates toArtifactCoordinates(Dependency dependency) {
    return new ArtifactCoordinates(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), dependency.getType(), dependency.getClassifier(), dependency.getScope());
  }


}

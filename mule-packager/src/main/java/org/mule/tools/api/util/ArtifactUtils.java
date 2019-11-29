/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.util;

import org.apache.maven.model.Dependency;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;

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
   * Converts a {@link ArtifactCoordinates} instance to a {@link org.apache.maven.model.Dependency} instance.
   *
   * @param artifactCoordinates the artifact coordinates to be converted.
   * @return the corresponding {@link org.apache.maven.model.Dependency} instance.
   */
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

  /**
   * Converts a {@link org.apache.maven.model.Dependency} instance to a {@link ArtifactCoordinates} instance.
   *
   * @param dependency the dependency to be converted.
   * @return the corresponding {@link ArtifactCoordinates} instance.
   */
  public static ArtifactCoordinates toArtifactCoordinates(Dependency dependency) {
    return new ArtifactCoordinates(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(),
                                   dependency.getType(), dependency.getClassifier(), dependency.getScope());
  }

  /**
   * Converts a {@link ArtifactCoordinates} instance to a {@link org.mule.maven.client.api.model.BundleDescriptor} instance.
   *
   * @param artifactCoordinates the artifact coordinates to be converted.
   * @return the corresponding {@link org.mule.maven.client.api.model.BundleDescriptor} instance.
   */
  public static BundleDescriptor toBundleDescriptor(ArtifactCoordinates artifactCoordinates) {
    return new BundleDescriptor.Builder()
        .setGroupId(artifactCoordinates.getGroupId())
        .setArtifactId(artifactCoordinates.getArtifactId())
        .setVersion(artifactCoordinates.getVersion())
        .setBaseVersion(artifactCoordinates.getVersion())
        .setClassifier(artifactCoordinates.getClassifier())
        .setType(artifactCoordinates.getType()).build();
  }

}

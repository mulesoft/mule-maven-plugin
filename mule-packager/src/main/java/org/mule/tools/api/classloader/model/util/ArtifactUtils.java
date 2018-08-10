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

import static org.mule.maven.client.internal.AetherMavenClient.MULE_PLUGIN_CLASSIFIER;
import static org.mule.maven.client.internal.util.MavenUtils.getPomModelFromFile;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;

/**
 * ArtifactUtils presents helper methods to convert artifact related classes and recognize mule plugin artifacts.
 */
public class ArtifactUtils {

  private static final String PACKAGE_TYPE = "jar";
  private static final String PROVIDED = "provided";
  private static final URI EMPTY_RESOURCE = URI.create("");
  private static final String POM_TYPE = "pom";

  /**
   * Convert a {@link BundleDescriptor} instance to {@link ArtifactCoordinates}.
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
   * Convert a {@link BundleDependency} instance to {@link Artifact}.
   *
   * @param bundleDependency the bundle dependency to be converted.
   * @return the corresponding artifact with normalized version.
   */
  public static Artifact toArtifact(BundleDependency bundleDependency) {
    ArtifactCoordinates artifactCoordinates = toArtifactCoordinates(bundleDependency.getDescriptor());
    return new Artifact(artifactCoordinates, bundleDependency.getBundleUri());
  }

  /**
   * Converts a {@link List<BundleDependency>} to a {@link List<Artifact>}.
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
   * Converts a {@link ArtifactCoordinates} instance to a {@link BundleDescriptor} instance.
   *
   * @param artifactCoordinates the artifact coordinates to be converted.
   * @return the corresponding {@link BundleDescriptor} instance.
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


  public static List<Artifact> toApplicationModelArtifacts(List<BundleDependency> appDependencies) {
    List<Artifact> dependencies = toArtifacts(appDependencies);
    dependencies.forEach(ArtifactUtils::updateScopeIfDomain);
    return dependencies;
  }

  public static void updateScopeIfDomain(Artifact artifact) {
    String classifier = artifact.getArtifactCoordinates().getClassifier();
    if (StringUtils.equals(classifier, MULE_DOMAIN.toString())) {
      artifact.getArtifactCoordinates().setScope(PROVIDED);
      artifact.setUri(EMPTY_RESOURCE);
    }
  }


  public static ArtifactCoordinates getApplicationArtifactCoordinates(File pomFile) {
    ArtifactCoordinates appCoordinates = toArtifactCoordinates(getPomProjectBundleDescriptor(pomFile));
    appCoordinates.setType(PACKAGE_TYPE);
    appCoordinates.setClassifier(getPomModelFromFile(pomFile).getPackaging());
    return appCoordinates;
  }

  public static BundleDescriptor getPomProjectBundleDescriptor(File pomFile) {
    Model pomModel = getPomModelFromFile(pomFile);
    return getBundleDescriptor(pomModel);
  }


  public static BundleDescriptor getBundleDescriptor(Model pomModel) {
    final String version =
        StringUtils.isNotBlank(pomModel.getVersion()) ? pomModel.getVersion() : pomModel.getParent().getVersion();
    return new BundleDescriptor.Builder()
        .setGroupId(StringUtils.isNotBlank(pomModel.getGroupId()) ? pomModel.getGroupId() : pomModel.getParent().getGroupId())
        .setArtifactId(pomModel.getArtifactId())
        .setVersion(version)
        .setBaseVersion(version)
        .setType(POM_TYPE)
        .build();
  }
}

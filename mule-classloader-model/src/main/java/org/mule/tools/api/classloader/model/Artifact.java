/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.io.File.separatorChar;
import static org.apache.commons.io.FilenameUtils.normalize;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.mule.tools.api.classloader.util.ArrayUtils;

@Getter
@Builder
public class Artifact implements Comparable<Object> {

  private static final String MULE_DOMAIN = "mule-domain";

  /**
   * Resolves an artifact full path in a given repository based on the default maven repository layout.
   *
   * @param outputDirectory the directory that is going to have its path prepended to the formatted output directory.
   * @param artifactCoordinates the artifact coordinates that from which the formatted output directory is going to be
   *        constructed.
   * @return the formatted artifact path.
   */
  public static File getFormattedMavenDirectory(File outputDirectory, ArtifactCoordinates artifactCoordinates) {
    String path = new StringBuilder()
        .append(artifactCoordinates.getGroupId().replace('.', separatorChar))
        .append(separatorChar)
        .append(artifactCoordinates.getArtifactId())
        .append(separatorChar)
        .append(artifactCoordinates.getVersion())
        .append(separatorChar)
        .toString();
    return new File(outputDirectory, path);
  }

  /**
   * Build the default artifact file name in a maven repository.
   *
   * @param artifact the artifact from which the default file name is going to be resolved.
   * @return the default artifact file name that a repository resource contains given its type.
   */
  public static String getFormattedArtifactFileName(Artifact artifact) {
    ArtifactCoordinates artifactCoordinates = artifact.getArtifactCoordinates();
    return buildMainFileName(artifactCoordinates) + "." + artifactCoordinates.getType();
  }

  /**
   * Build the main main artifact file name in a maven repository.
   *
   * @param artifactCoordinates the artifact coordinates from which the main file name is going to be resolved.
   * @return the main artifact file name that a repository resource contains without its type extension.
   */
  private static String buildMainFileName(ArtifactCoordinates artifactCoordinates) {
    String version = "-" + artifactCoordinates.getVersion();
    String classifier = Optional.ofNullable(artifactCoordinates.getClassifier())
        .filter(StringUtils::isNotBlank)
        .map(value -> "-" + value)
        .orElse(StringUtils.EMPTY);
    return artifactCoordinates.getArtifactId() + version + classifier;
  }

  /**
   * Build the default pom file name in a maven repository.
   *
   * @param artifact the artifact from which the default pom file name is going to be resolved.
   * @return the default pom file name that a repository resource contains given an artifact.
   */
  public static String getPomFileName(Artifact artifact) {
    return buildMainPOMFileName(artifact.getArtifactCoordinates()) + ".pom";
  }

  /**
   * Build the main pom file name in a maven repository.
   *
   * @param artifactCoordinates the artifact coordinates from which the main pom file name is going to be resolved.
   * @return the main pom file name that a repository resource contains without its pom extension.
   */
  public static String buildMainPOMFileName(ArtifactCoordinates artifactCoordinates) {
    return artifactCoordinates.getArtifactId() + "-" + artifactCoordinates.getVersion();
  }

  private final ArtifactCoordinates artifactCoordinates;
  private final URI uri;
  private final Boolean isShared;
  private final String[] packages;
  private final String[] resources;

  public Artifact(ArtifactCoordinates artifactCoordinates, URI uri) {
    this(artifactCoordinates, uri, false, new String[0], new String[0]);
  }

  public Artifact(ArtifactCoordinates artifactCoordinates, URI uri, Boolean isShared, String[] packages, String[] resources) {
    checkNotNull(artifactCoordinates, "ArtifactCoordinates cannot be null");
    if (!StringUtils.equals(MULE_DOMAIN, artifactCoordinates.getClassifier())) {
      checkNotNull(uri, "Uri cannot be null");
    }

    this.artifactCoordinates = artifactCoordinates;
    this.uri = uri;
    this.isShared = isShared;
    this.packages = Optional.ofNullable(packages).orElseGet(() -> new String[0]);
    this.resources = Optional.ofNullable(resources).orElseGet(() -> new String[0]);
  }

  public Artifact setUri(URI uri) {
    if (!StringUtils.equals(MULE_DOMAIN, artifactCoordinates.getClassifier())) {
      checkNotNull(uri, "Uri cannot be null");
    }
    return getCopyBuilder()
        .uri(uri)
        .build();
  }

  private Artifact setArtifactCoordinates(ArtifactCoordinates artifactCoordinates) {
    checkNotNull(artifactCoordinates, "Artifact coordinates cannot be null");
    return getCopyBuilder()
        .artifactCoordinates(artifactCoordinates)
        .build();
  }

  /**
   * Sets a boolean representing if the artifact will be shared with the app's plugins.
   * @param isShared true if the artifact should be shared.
   */
  public Artifact setShared(boolean isShared) {
    return getCopyBuilder()
        .isShared(isShared)
        .build();
  }

  public Artifact setPackages(String[] packages) {
    return getCopyBuilder()
        .packages(Optional.ofNullable(packages).orElseGet(() -> new String[0]))
        .build();
  }

  public Artifact setResources(String[] resources) {
    return getCopyBuilder()
        .resources(Optional.ofNullable(resources).orElseGet(() -> new String[0]))
        .build();
  }

  @Override
  public String toString() {
    return String.valueOf(artifactCoordinates);
  }

  @Override
  public int compareTo(@Nonnull Object that) {
    return String.valueOf(artifactCoordinates).compareTo(String.valueOf(that));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Artifact)) {
      return false;
    }

    Artifact that = (Artifact) o;
    return artifactCoordinates.equals(that.artifactCoordinates);
  }

  @Override
  public int hashCode() {
    return artifactCoordinates.hashCode();
  }

  public Artifact copyWithParameterizedUri() {
    if (StringUtils.equals(artifactCoordinates.getClassifier(), MULE_DOMAIN)) {
      return new Artifact(artifactCoordinates, uri);
    }
    String fileName = getFormattedArtifactFileName(this);
    String path = getFormattedMavenDirectory(new File("repository"), artifactCoordinates).getPath();
    try {
      URI uri = new URI(normalize(new File(path, fileName).getPath(), true));
      return getCopyBuilder()
          .uri(uri)
          .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException("Could not generate URI for resource, the given path is invalid: " + path, e);
    }
  }

  private Artifact.ArtifactBuilder getCopyBuilder() {
    return Artifact.builder()
        .artifactCoordinates(artifactCoordinates)
        .uri(uri)
        .isShared(isShared)
        .packages(ArrayUtils.copyOf(packages, Function.identity()))
        .resources(ArrayUtils.copyOf(resources, Function.identity()));
  }

  /**
   * Build the default pom file name in a maven repository.
   *
   * @return the default pom file name that a repository resource contains given an artifact.
   */
  public String getPomFileName() {
    return getPomFileName(this);
  }

  public String getFormattedArtifactFileName() {
    return getFormattedArtifactFileName(this);
  }

  public File getFormattedMavenDirectory(File repositoryFile) {
    return getFormattedMavenDirectory(repositoryFile, artifactCoordinates);
  }

  public Boolean isShared() {
    return isShared;
  }
}

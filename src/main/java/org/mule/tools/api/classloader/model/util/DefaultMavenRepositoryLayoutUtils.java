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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;

import java.io.File;

import static java.io.File.separatorChar;

/**
 * DefaultMavenRepositoryUtils knows how to build the artifact resource path based on the artifact coordinates and the default
 * maven layout structure convention.
 *
 */
public class DefaultMavenRepositoryLayoutUtils {

  /**
   * Build the default pom file name in a maven repository.
   *
   * @param artifact the artifact from which the default pom file name is going to be resolved.
   * @return the default pom file name that a repository resource contains given an artifact.
   */
  public static String getPomFileName(Artifact artifact) {
    return buildMainPOMFileName(artifact) + ".pom";
  }

  /**
   * Build the default artifact file name in a maven repository.
   *
   * @param artifact the artifact from which the default file name is going to be resolved.
   * @return the default artifact file name that a repository resource contains given its type.
   */
  public static String getFormattedFileName(Artifact artifact) {
    String destFileName = buildMainFileName(artifact);
    String extension = new DefaultArtifactHandler(artifact.getType()).getExtension();
    return destFileName + "." + extension;
  }

  /**
   * Build the main main artifact file name in a maven repository.
   *
   * @param artifact the artifact from which the main file name is going to be resolved.
   * @return the main artifact file name that a repository resource contains without its type extension.
   */
  private static String buildMainFileName(Artifact artifact) {
    String versionString = "-" + getNormalizedVersion(artifact);
    String classifierString = StringUtils.EMPTY;
    if (StringUtils.isNotBlank(artifact.getClassifier())) {
      classifierString = "-" + artifact.getClassifier();
    }
    return artifact.getArtifactId() + versionString + classifierString;
  }

  /**
   * Build the main pom file name in a maven repository.
   *
   * @param artifact the artifact from which the main pom file name is going to be resolved.
   * @return the main pom file name that a repository resource contains without its pom extension.
   */
  public static String buildMainPOMFileName(Artifact artifact) {
    String versionString = "-" + getNormalizedVersion(artifact);
    return artifact.getArtifactId() + versionString;
  }

  /**
   * Resolves snapshot versions substituting the timestamp by SNAPSHOT.
   *
   * @param artifact the artifact that is going to have its version normalized.
   * @return the normalized artifact version.
   */
  public static String getNormalizedVersion(Artifact artifact) {
    if (artifact.isSnapshot() && !artifact.getVersion().equals(artifact.getBaseVersion())) {
      return artifact.getBaseVersion();
    }
    return artifact.getVersion();
  }

  /**
   * Resolves an artifact full path in a given repository based on the default maven repository layout.
   *
   * @param outputDirectory the directory that is going to have its path prepended to the formatted output directory.
   * @param artifact the artifact that from which the formatted output directory is going to be constructed.
   * @return the formatted artifact path.
   */
  public static File getFormattedOutputDirectory(File outputDirectory, Artifact artifact) {
    StringBuilder sb = new StringBuilder();

    sb.append(artifact.getGroupId().replace('.', separatorChar)).append(separatorChar);
    sb.append(artifact.getArtifactId()).append(separatorChar);
    sb.append(artifact.getBaseVersion()).append(separatorChar);

    return new File(outputDirectory, sb.toString());
  }
}

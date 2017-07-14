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

public class DefaultMavenRepositoryLayoutUtils {

  public static String getPomFileName(Artifact artifact) {
    StringBuilder destFileName = buildMainPOMFileName(artifact);

    destFileName.append("pom");

    return destFileName.toString();
  }

  public static String getFormattedFileName(Artifact artifact) {
    StringBuilder destFileName = buildMainFileName(artifact);

    String extension = new DefaultArtifactHandler(artifact.getType()).getExtension();
    destFileName.append(extension);

    return destFileName.toString();
  }

  public static StringBuilder buildMainFileName(Artifact artifact) {
    StringBuilder mainName = new StringBuilder();
    String versionString = "-" + getNormalizedVersion(artifact);
    String classifierString = StringUtils.EMPTY;

    if (StringUtils.isNotBlank(artifact.getClassifier())) {
      classifierString = "-" + artifact.getClassifier();
    }
    mainName.append(artifact.getArtifactId()).append(versionString);
    mainName.append(classifierString).append(".");
    return mainName;
  }

  public static StringBuilder buildMainPOMFileName(Artifact artifact) {
    StringBuilder mainName = new StringBuilder();
    String versionString = "-" + getNormalizedVersion(artifact);

    mainName.append(artifact.getArtifactId()).append(versionString);
    mainName.append(".");
    return mainName;
  }

  public static String getNormalizedVersion(Artifact artifact) {
    if (artifact.isSnapshot() && !artifact.getVersion().equals(artifact.getBaseVersion())) {
      return artifact.getBaseVersion();
    }
    return artifact.getVersion();
  }

  public static File getFormattedOutputDirectory(File outputDirectory, Artifact artifact) {
    StringBuilder sb = new StringBuilder();
    sb.append(artifact.getGroupId().replace('.', separatorChar)).append(separatorChar);
    sb.append(artifact.getArtifactId()).append(separatorChar);
    sb.append(artifact.getBaseVersion()).append(separatorChar);

    return new File(outputDirectory, sb.toString());
  }
}

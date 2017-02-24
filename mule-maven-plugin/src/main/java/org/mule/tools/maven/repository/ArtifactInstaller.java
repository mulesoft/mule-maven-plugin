/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.repository;

import static java.io.File.separatorChar;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.mule.tools.artifact.archiver.api.PackagerConstants.REPOSITORY_FOLDER;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class ArtifactInstaller {

  private static final int MAX_NAME_SIZE = 128;
  private Log log;

  public ArtifactInstaller(Log log) {
    this.log = log;
  }

  public void installArtifact(File repositoryFile, Artifact artifact) throws MojoExecutionException {
    String artifactFilename = getFormattedFileName(artifact);
    File artifactFolderDestination = getFormattedOutputDirectory(repositoryFile, artifact);

    if (!artifactFolderDestination.exists()) {
      artifactFolderDestination.mkdirs();
    }

    File destinationArtifactFile = new File(artifactFolderDestination, artifactFilename);
    try {
      log.info(format("Adding artifact <%s%s>",
                      REPOSITORY_FOLDER,
                      destinationArtifactFile.getAbsolutePath()
                          .replaceFirst(Pattern.quote(repositoryFile.getAbsolutePath()),
                                        "")));
      copyFile(artifact.getFile(), destinationArtifactFile);
    } catch (IOException e) {
      throw new MojoExecutionException(
                                       format("There was a problem while copying the artifact [%s] file [%s] to the destination [%s]",
                                              artifact.toString(), artifact.getFile().getAbsolutePath(),
                                              destinationArtifactFile.getAbsolutePath()),
                                       e);
    }
  }

  private String getFormattedFileName(Artifact artifact) {
    StringBuilder destFileName = new StringBuilder();
    String versionString = "-" + getNormalizedVersion(artifact);
    String classifierString = StringUtils.EMPTY;

    if (artifact.getClassifier() != null && !artifact.getClassifier().isEmpty()) {
      classifierString = "-" + artifact.getClassifier();
    }
    destFileName.append(artifact.getArtifactId()).append(versionString);
    destFileName.append(classifierString).append(".");
    destFileName.append(artifact.getArtifactHandler().getExtension());

    return destFileName.toString();
  }

  private String getNormalizedVersion(Artifact artifact) {
    if (artifact.isSnapshot() && !artifact.getVersion().equals(artifact.getBaseVersion())) {
      return artifact.getBaseVersion();
    }
    return artifact.getVersion();
  }

  private static File getFormattedOutputDirectory(File outputDirectory, Artifact artifact) {
    StringBuilder sb = new StringBuilder(MAX_NAME_SIZE);
    sb.append(artifact.getGroupId().replace('.', separatorChar)).append(separatorChar);
    sb.append(artifact.getArtifactId()).append(separatorChar);
    sb.append(artifact.getBaseVersion()).append(separatorChar);

    return new File(outputDirectory, sb.toString());
  }
}

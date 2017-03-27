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

import static com.google.common.base.Preconditions.checkArgument;
import static java.io.File.separatorChar;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.REPOSITORY;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class ArtifactInstaller {

  private Log log;

  public ArtifactInstaller(Log log) {
    this.log = log;
  }

  public void installArtifact(File repositoryFile, Artifact artifact) throws MojoExecutionException {
    checkArgument(artifact != null, "Artifact to be installed should not be null");
    String artifactFilename = getFormattedFileName(artifact);
    File artifactFolderDestination = getFormattedOutputDirectory(repositoryFile, artifact);

    if (!artifactFolderDestination.exists()) {
      artifactFolderDestination.mkdirs();
    }

    File destinationArtifactFile = new File(artifactFolderDestination, artifactFilename);
    try {
      log.info(format("Adding artifact <%s%s>",
                      REPOSITORY,
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

  protected String getFormattedFileName(Artifact artifact) {
    StringBuilder destFileName = new StringBuilder();
    String versionString = "-" + getNormalizedVersion(artifact);
    String classifierString = StringUtils.EMPTY;

    if (artifact.getClassifier() != null && !artifact.getClassifier().isEmpty()) {
      classifierString = "-" + artifact.getClassifier();
    }
    destFileName.append(artifact.getArtifactId()).append(versionString);
    destFileName.append(classifierString).append(".");

    Optional<ArtifactHandler> artifactHandler = Optional.ofNullable(artifact.getArtifactHandler());
    String extension = artifactHandler.orElse(new DefaultArtifactHandler(artifact.getType())).getExtension();
    destFileName.append(extension);

    return destFileName.toString();
  }

  protected String getNormalizedVersion(Artifact artifact) {
    if (artifact.isSnapshot() && !artifact.getVersion().equals(artifact.getBaseVersion())) {
      return artifact.getBaseVersion();
    }
    return artifact.getVersion();
  }

  protected static File getFormattedOutputDirectory(File outputDirectory, Artifact artifact) {
    StringBuilder sb = new StringBuilder();
    sb.append(artifact.getGroupId().replace('.', separatorChar)).append(separatorChar);
    sb.append(artifact.getArtifactId()).append(separatorChar);
    sb.append(artifact.getBaseVersion()).append(separatorChar);

    return new File(outputDirectory, sb.toString());
  }
}

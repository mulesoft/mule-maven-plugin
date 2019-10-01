/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.repository;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.mule.tools.api.packager.structure.FolderNames.REPOSITORY;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.packager.sources.MuleContentGenerator;
import org.mule.tools.api.util.PackagerLog;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public class ArtifactInstaller {

  private static final String POM_FILE_NAME = "pom.xml";
  private PackagerLog log;

  public ArtifactInstaller(PackagerLog log) {
    this.log = log;
  }

  @Deprecated
  public void installArtifact(File repositoryFile, Artifact artifact, Optional<ClassLoaderModel> classLoaderModel)
      throws IOException {
    installArtifact(repositoryFile, artifact, classLoaderModel, true);
  }

  public void installArtifact(File repositoryFile, Artifact artifact, Optional<ClassLoaderModel> classLoaderModel,
                              boolean prettyPrinting)
      throws IOException {
    checkArgument(artifact != null, "Artifact to be installed should not be null");
    File artifactFolderDestination = artifact.getFormattedMavenDirectory(repositoryFile);

    if (!artifactFolderDestination.exists()) {
      artifactFolderDestination.mkdirs();
    }

    try {
      generateArtifactFile(artifact, artifactFolderDestination, repositoryFile);
      generateDependencyDescriptorFile(artifact, artifactFolderDestination, classLoaderModel, prettyPrinting);
    } catch (IOException e) {
      throw new IOException(
                            format("There was a problem while copying the artifact [%s] file [%s] to the application local repository",
                                   artifact.toString(), artifact.getUri().getPath()),
                            e);
    }
  }

  protected void generateArtifactFile(Artifact artifact, File artifactFolderDestination, File repositoryFile) throws IOException {
    String artifactFilename = artifact.getFormattedArtifactFileName();

    File destinationArtifactFile = new File(artifactFolderDestination, artifactFilename);
    log.debug(format("Adding artifact <%s%s>",
                     REPOSITORY.value(),
                     destinationArtifactFile.getAbsolutePath()
                         .replaceFirst(Pattern.quote(repositoryFile.getAbsolutePath()),
                                       "")));

    copyFile(new File(artifact.getUri()), destinationArtifactFile);
  }

  @Deprecated
  protected void generateDependencyDescriptorFile(Artifact artifact, File artifactFolderDestination,
                                                  Optional<ClassLoaderModel> classLoaderModel)
      throws IOException {
    generateDependencyDescriptorFile(artifact, artifactFolderDestination, classLoaderModel, true);
  }

  protected void generateDependencyDescriptorFile(Artifact artifact, File artifactFolderDestination,
                                                  Optional<ClassLoaderModel> classLoaderModel, boolean prettyPrinting)
      throws IOException {
    if (classLoaderModel.isPresent()) {
      generateClassloderModelFile(classLoaderModel.get(), artifactFolderDestination, prettyPrinting);
    } else {
      generatePomFile(artifact, artifactFolderDestination);
    }
  }

  protected void generatePomFile(Artifact artifact, File artifactFolderDestination) throws IOException {
    String artifactPomFilename = artifact.getPomFileName();
    File srcPomFolder = new File(artifact.getUri()).getParentFile();
    File srcPomFile = new File(srcPomFolder, artifactPomFilename);
    File destinationPomFile = new File(artifactFolderDestination, artifactPomFilename);
    if (!srcPomFile.exists()) {
      srcPomFile = new File(srcPomFolder, POM_FILE_NAME);
    }
    copyFile(srcPomFile, destinationPomFile);
  }

  @Deprecated
  protected void generateClassloderModelFile(ClassLoaderModel classLoaderModel, File artifactFolderDestination) {
    generateClassloderModelFile(classLoaderModel, artifactFolderDestination, true);
  }

  protected void generateClassloderModelFile(ClassLoaderModel classLoaderModel, File artifactFolderDestination,
                                             boolean prettyPrinting) {
    MuleContentGenerator.createClassLoaderModelJsonFile(classLoaderModel, artifactFolderDestination, prettyPrinting);
  }
}

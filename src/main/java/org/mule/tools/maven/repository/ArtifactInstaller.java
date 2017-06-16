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
import static org.mule.tools.api.packager.PackagerFolders.REPOSITORY;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

public class ArtifactInstaller {

  private final List<ArtifactRepository> remoteRepositories;
  private Log log;
  private RepositorySystem repositorySystem;
  private RepositorySystemSession repositorySystemSession;

  public ArtifactInstaller(Log log, List<ArtifactRepository> remoteArtifactRepositories, RepositorySystem repositorySystem,
                           RepositorySystemSession repositorySystemSession) {
    this.log = log;
    this.remoteRepositories = remoteArtifactRepositories;
    this.repositorySystem = repositorySystem;
    this.repositorySystemSession = repositorySystemSession;
  }

  protected List<RemoteRepository> toAetherRepos(List<ArtifactRepository> remoteArtifactRepositories) {
    return RepositoryUtils.toRepos(remoteArtifactRepositories);
  }

  public void installArtifact(File repositoryFile, Artifact artifact) throws MojoExecutionException {
    checkArgument(artifact != null, "Artifact to be installed should not be null");
    String artifactFilename = getFormattedFileName(artifact);
    String artifactPomFilename = getPomFileName(artifact);
    File artifactFolderDestination = getFormattedOutputDirectory(repositoryFile, artifact);
    if (!artifactFolderDestination.exists()) {
      artifactFolderDestination.mkdirs();
    }
    File destinationArtifactFile = new File(artifactFolderDestination, artifactFilename);
    File destinationPomFile = new File(artifactFolderDestination, artifactPomFilename);
    try {

      log.info(format("Adding artifact <%s%s>",
                      REPOSITORY,
                      destinationArtifactFile.getAbsolutePath()
                          .replaceFirst(Pattern.quote(repositoryFile.getAbsolutePath()),
                                        "")));

      copyFile(artifact.getFile(), destinationArtifactFile);
      copyFile(new File(artifact.getFile().getParent(), artifactPomFilename), destinationPomFile);
    } catch (IOException e) {
      throw new MojoExecutionException(
                                       format("There was a problem while copying the artifact [%s] file [%s] to the destination [%s]",
                                              artifact.toString(), artifact.getFile().getAbsolutePath(),
                                              destinationArtifactFile.getAbsolutePath()),
                                       e);
    }
  }

  private String getPomFileName(Artifact artifact) {
    StringBuilder destFileName = buildMainPOMFileName(artifact);

    destFileName.append("pom");

    return destFileName.toString();
  }

  protected String getFormattedFileName(Artifact artifact) {
    StringBuilder destFileName = buildMainFileName(artifact);

    String extension = new DefaultArtifactHandler(artifact.getType()).getExtension();
    destFileName.append(extension);

    return destFileName.toString();
  }

  private StringBuilder buildMainFileName(Artifact artifact) {
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

  private StringBuilder buildMainPOMFileName(Artifact artifact) {
    StringBuilder mainName = new StringBuilder();
    String versionString = "-" + getNormalizedVersion(artifact);

    mainName.append(artifact.getArtifactId()).append(versionString);
    mainName.append(".");
    return mainName;
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

  public void downloadExcludedDependencyToLocalRepository(Artifact artifact) throws MojoExecutionException {
    CollectRequest collectRequest = new CollectRequest();
    collectRequest.setRoot(new Dependency(RepositoryUtils.toArtifact(artifact), null));
    for (RemoteRepository repository : toAetherRepos(remoteRepositories)) {
      collectRequest.addRepository(repository);
    }
    DependencyFilter dependencyFilter = DependencyFilterUtils
        .classpathFilter(JavaScopes.RUNTIME, JavaScopes.COMPILE, JavaScopes.PROVIDED, JavaScopes.SYSTEM, JavaScopes.TEST);
    DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, dependencyFilter);
    try {
      DependencyResult dependencyResult = repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);
      List<ArtifactResult> results = dependencyResult.getArtifactResults();
      if (results != null && results.size() != 0) {
        org.eclipse.aether.artifact.Artifact resolved = results.get(0).getArtifact();
        artifact.setFile(resolved.getFile());
      }
    } catch (DependencyResolutionException e) {
      throw new MojoExecutionException("Error while resolving dependency " + artifact.getGroupId() + ":"
          + artifact.getArtifactId() + ":" + artifact.getVersion(), e);
    }
  }

  public void generateExcludedDependencyMetadata(File repositoryFile, Artifact artifact) throws MojoExecutionException {
    checkArgument(artifact != null, "Artifact from which metadata is going to be extracted should not be null");
    String pomFileName = getPomFileName(artifact);
    File metadataFolderDestination = getFormattedOutputDirectory(repositoryFile, artifact);

    if (!metadataFolderDestination.exists()) {
      metadataFolderDestination.mkdirs();
    }
    if (artifact.getFile() == null) {
      return;
    }
    File sourcePomFile = new File(artifact.getFile().getParent(), pomFileName);
    File destinationPomFile = new File(metadataFolderDestination, pomFileName);
    try {
      log.info(format("Adding artifact <%s%s>",
                      REPOSITORY,
                      destinationPomFile.getAbsolutePath()
                          .replaceFirst(Pattern.quote(repositoryFile.getAbsolutePath()),
                                        "")));
      copyFile(sourcePomFile, destinationPomFile);
    } catch (IOException e) {
      throw new MojoExecutionException(
                                       format("There was a problem while copying the [%s] pom file from [%s] to the destination [%s]",
                                              artifact.toString(), sourcePomFile.getAbsolutePath(),
                                              destinationPomFile.getAbsolutePath()),
                                       e);
    }
  }
}

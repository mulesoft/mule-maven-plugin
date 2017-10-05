/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.aether.repository.RemoteRepository;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.maven.utils.ArtifactUtils;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.resources.content.ResourcesContent;
import org.mule.tools.api.packager.sources.ContentGenerator;
import org.mule.tools.api.packager.sources.ContentGeneratorFactory;
import org.mule.tools.api.repository.MuleMavenPluginClientProvider;


/**
 * Base Mojo
 */
public abstract class AbstractMuleMojo extends AbstractMojo {

  @Component
  protected ProjectBuilder projectBuilder;

  @Component
  protected RepositorySystem repositorySystem;

  @Parameter(readonly = true, required = true, defaultValue = "${session}")
  protected MavenSession session;

  @Parameter(readonly = true, required = true, defaultValue = "${localRepository}")
  protected ArtifactRepository localRepository;

  @Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
  protected List<ArtifactRepository> remoteArtifactRepositories;

  @Parameter(property = "project", required = true)
  protected MavenProject project;

  @Parameter(property = "project.build.directory", required = true)
  protected File outputDirectory;

  @Parameter(defaultValue = "${project.basedir}")
  protected File projectBaseFolder;

  @Parameter(defaultValue = "${lightweightPackage}")
  protected boolean lightweightPackage = false;

  @Parameter(defaultValue = "${skipValidation}")
  protected boolean skipValidation = false;

  @Parameter(property = "shared.libraries")
  protected List<org.mule.tools.api.classloader.model.SharedLibraryDependency> sharedLibraries;

  @Parameter(defaultValue = "${testJar}")
  protected boolean testJar = false;

  @Parameter
  protected String classifier;

  protected ContentGenerator contentGenerator;

  protected static ResourcesContent resourcesContent;

  protected AetherMavenClient aetherMavenClient;
  protected ProjectInformation projectInformation;

  protected AetherMavenClient getAetherMavenClient() {
    if (aetherMavenClient == null) {
      List<RemoteRepository> remoteRepositories = RepositoryUtils.toRepos(remoteArtifactRepositories);
      aetherMavenClient = new MuleMavenPluginClientProvider(remoteRepositories,
                                                            getLog())
                                                                .buildMavenClient();
    }
    return aetherMavenClient;
  }

  protected List<ArtifactCoordinates> toArtifactCoordinates(List<Dependency> dependencies) {
    return dependencies.stream().map(ArtifactUtils::toArtifactCoordinates).collect(Collectors.toList());
  }

  protected ProjectInformation getProjectInformation() {
    if (projectInformation == null) {
      projectInformation = new ProjectInformation.Builder()
          .withGroupId(project.getGroupId())
          .withArtifactId(project.getArtifactId())
          .withVersion(project.getVersion())
          .withPackaging(project.getPackaging())
          .withProjectBaseFolder(Paths.get(projectBaseFolder.toURI()))
          .withBuildDirectory(Paths.get(project.getBuild().getDirectory()))
          .setTestProject(testJar)
          .build();
    }
    return projectInformation;
  }

  public ContentGenerator getContentGenerator() {
    if (contentGenerator == null) {
      contentGenerator = ContentGeneratorFactory.create(getProjectInformation());
    }
    return contentGenerator;
  }
}

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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.mule.tools.api.ContentGenerator;
import org.mule.tools.api.packager.PackagingType;
import org.mule.tools.maven.mojo.model.SharedLibraryDependency;


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
  protected List<SharedLibraryDependency> sharedLibraries;

  @Parameter
  protected String classifier;

  protected ContentGenerator contentGenerator;

  protected ContentGenerator getContentGenerator() {
    if (contentGenerator == null) {
      contentGenerator = new ContentGenerator(project.getGroupId(), project.getArtifactId(), project.getVersion(),
                                              PackagingType.fromString(project.getPackaging()),
                                              Paths.get(projectBaseFolder.toURI()), Paths.get(project.getBuild().getDirectory()));
    }
    return contentGenerator;
  }
}

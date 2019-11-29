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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.mule.tools.api.packager.resources.content.ResourcesContent;
import org.mule.tools.api.packager.sources.ContentGenerator;
import org.mule.tools.api.packager.sources.ContentGeneratorFactory;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.validation.resolver.MulePluginResolver;
import org.mule.tools.api.util.MavenProjectBuilder;


/**
 * Base Mojo
 */
public abstract class AbstractMuleMojo extends AbstractGenericMojo {

  @Component
  protected ProjectBuilder projectBuilder;

  @Component
  protected RepositorySystem repositorySystem;

  @Parameter(property = "project.build.directory", required = true)
  protected File outputDirectory;

  @Parameter(defaultValue = "${skipValidation}")
  protected boolean skipValidation = false;

  protected ContentGenerator contentGenerator;

  protected static ResourcesContent resourcesContent;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!hasExecutedBefore()) {
      initMojo();
      doExecute();
    } else {
      getLog().debug("Skipping execution because it has already been run");
    }
  }

  public ContentGenerator getContentGenerator() {
    if (contentGenerator == null) {
      contentGenerator = ContentGeneratorFactory.create(getProjectInformation());
    }
    return contentGenerator;
  }

  protected MulePluginResolver getResolver(Project project) {
    MavenProjectBuilder builder = new MavenProjectBuilder(getLog(), session, projectBuilder, repositorySystem, localRepository,
                                                          remoteArtifactRepositories);
    return new MulePluginResolver(builder, project);
  }
}

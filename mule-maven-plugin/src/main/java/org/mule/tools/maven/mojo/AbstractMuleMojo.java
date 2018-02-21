/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
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
import org.mule.tools.api.packager.sources.ContentGenerator;
import org.mule.tools.api.packager.sources.MuleContentGenerator;


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

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!hasExecutedBefore()) {
      // initMojo();
      doExecute();
    } else {
      getLog().debug("Skipping execution because it has already been run");
    }
  }

  public ContentGenerator getContentGenerator() {
    if (contentGenerator == null) {
      contentGenerator = new MuleContentGenerator(getAndSetProjectInformation());
    }
    return contentGenerator;
  }
}

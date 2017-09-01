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

import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.packager.sources.MuleContentGenerator;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
@Mojo(name = "compile",
    defaultPhase = LifecyclePhase.COMPILE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CompileMojo extends AbstractMuleMojo {

  public void execute() throws MojoExecutionException, MojoFailureException {
    long start = System.currentTimeMillis();
    getLog().debug("Generating mule source code...");

    try {
      getContentGenerator().createMuleSrcFolderContent();
    } catch (IllegalArgumentException | IOException e) {
      throw new MojoFailureException("Fail to generate sources", e);
    }

    getLog().debug(MessageFormat.format("Source code generation done ({0}ms)", System.currentTimeMillis() - start));
  }

  protected MuleContentGenerator getContentGenerator() {
    return new MuleContentGenerator(project.getGroupId(), project.getArtifactId(), project.getVersion(),
                                    PackagingType.fromString(project.getPackaging()),
                                    Paths.get(projectBaseFolder.toURI()), Paths.get(project.getBuild().getDirectory()));
  }
}

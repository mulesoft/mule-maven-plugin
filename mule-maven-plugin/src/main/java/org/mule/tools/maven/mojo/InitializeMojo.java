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

import java.nio.file.Paths;
import java.text.MessageFormat;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.api.packager.AbstractProjectFoldersGenerator;
import org.mule.tools.api.packager.BundleDomainProjectFoldersGenerator;
import org.mule.tools.api.packager.MuleProjectFoldersGenerator;
import org.mule.tools.api.packager.packaging.PackagingType;

/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "initialize",
    defaultPhase = LifecyclePhase.INITIALIZE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class InitializeMojo extends AbstractMuleMojo {

  public void execute() throws MojoExecutionException, MojoFailureException {
    long start = System.currentTimeMillis();
    getLog().debug("Initializing Mule Maven Plugin...");

    getProjectFoldersGenerator().generate(Paths.get(project.getBuild().getDirectory()));

    getLog().debug(MessageFormat.format("Mule Maven Plugin Initialize done ({0}ms)", System.currentTimeMillis() - start));
  }

  protected AbstractProjectFoldersGenerator getProjectFoldersGenerator() {
    String groupId = project.getGroupId();
    String artifactId = project.getArtifactId();
    PackagingType packagingType = PackagingType.fromString(project.getPackaging());
    if (packagingType.equals(PackagingType.MULE_DOMAIN_BUNDLE)) {
      return new BundleDomainProjectFoldersGenerator(groupId, artifactId, packagingType);
    }
    return new MuleProjectFoldersGenerator(groupId, artifactId, packagingType);
  }
}

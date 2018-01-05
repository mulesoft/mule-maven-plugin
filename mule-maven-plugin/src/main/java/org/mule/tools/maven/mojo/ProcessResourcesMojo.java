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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.api.packager.resources.processor.DomainBundleProjectResourcesContentProcessor;
import org.mule.tools.api.packager.resources.processor.ResourcesContentProcessor;

@Mojo(name = "process-resources",
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ProcessResourcesMojo extends AbstractMuleMojo {

  @Override
  public void doExecute() throws MojoExecutionException, MojoFailureException {
    // try {
    // getResourcesContentProcessor().process(resourcesContent);
    // } catch (IllegalArgumentException | IOException e) {
    // throw new MojoFailureException("Fail to process resources", e);
    // }
  }

  public ResourcesContentProcessor getResourcesContentProcessor() {
    return new DomainBundleProjectResourcesContentProcessor(Paths.get(project.getBuild().getDirectory()));
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_PROCESS_RESOURCES_PREVIOUS_RUN_PLACEHOLDER";
  }
}

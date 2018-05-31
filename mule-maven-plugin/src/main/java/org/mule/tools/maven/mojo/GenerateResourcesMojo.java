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

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.api.packager.resources.generator.DomainBundleProjectResourcesContentGenerator;
import org.mule.tools.api.packager.resources.generator.ResourcesContentGenerator;

/**
 * Mojo that runs on the {@link LifecyclePhase#GENERATE_RESOURCES}
 */
@Mojo(name = "generate-resources",
    defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateResourcesMojo extends AbstractMuleMojo {

  @Override
  public void doExecute() throws MojoFailureException {
    try {
      resourcesContent = getResourcesContentGenerator().generate();
    } catch (IllegalArgumentException e) {
      throw new MojoFailureException("Fail to generate resources", e);
    }
  }

  public ResourcesContentGenerator getResourcesContentGenerator() {
    return new DomainBundleProjectResourcesContentGenerator(getAetherMavenClient(),
                                                            toArtifactCoordinates(project.getDependencies()));
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_GENERATE_RESOURCES_PREVIOUS_RUN_PLACEHOLDER";
  }
}

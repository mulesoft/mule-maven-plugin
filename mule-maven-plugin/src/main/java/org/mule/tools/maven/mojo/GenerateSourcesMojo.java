/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo;

import java.io.IOException;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Mojo that runs on the {@link LifecyclePhase#GENERATE_RESOURCES}
 */
@Mojo(name = "generate-sources",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateSourcesMojo extends AbstractMuleMojo {

  @Override
  public void doExecute() throws MojoFailureException {
    getLog().debug("Generating mule source code...");
    try {
      getContentGenerator().createContent();
    } catch (IllegalArgumentException | IOException e) {
      throw new MojoFailureException("Fail to generate sources", e);
    }
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_GENERATE_SOURCES_PREVIOUS_RUN_PLACEHOLDER";
  }
}

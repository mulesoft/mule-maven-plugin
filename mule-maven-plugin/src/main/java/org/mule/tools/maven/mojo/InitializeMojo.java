/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo;

import java.nio.file.Paths;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.api.packager.*;

/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "initialize",
    defaultPhase = LifecyclePhase.INITIALIZE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class InitializeMojo extends AbstractMuleMojo {

  @Override
  public void doExecute() {
    getLog().debug("Initializing Mule Maven Plugin...");
    getProjectFoldersGenerator().generate(Paths.get(project.getBuild().getDirectory()));
  }

  public AbstractProjectFoldersGenerator getProjectFoldersGenerator() {
    return ProjectFoldersGeneratorFactory.create(getProjectInformation());
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_INITIALIZE_PREVIOUS_RUN_PLACEHOLDER";
  }
}

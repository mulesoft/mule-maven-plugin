/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * @author Mulesoft Inc.
 * @since 3.1.0
 */
@Mojo(name = "clean",
    defaultPhase = LifecyclePhase.CLEAN,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CleanMojo extends AbstractMuleMojo {

  @Override
  public void doExecute() {}

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_CLEAN_PREVIOUS_RUN_PLACEHOLDER";
  }
}

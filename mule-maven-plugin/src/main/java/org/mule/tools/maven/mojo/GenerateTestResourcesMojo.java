/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Build a Mule application archive.
 * 
 * @since 3.1.0
 */
@Mojo(name = "generate-test-resources",
    defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateTestResourcesMojo extends AbstractMuleMojo {

  @Override
  public void doExecute() {}

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_GENERATE_TEST_RESOURCES_PREVIOUS_RUN_PLACEHOLDER";
  }
}

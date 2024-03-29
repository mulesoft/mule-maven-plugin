/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * @author Mulesoft Inc.
 * @since 3.1.0
 */
@Mojo(name = "test-compile",
    defaultPhase = LifecyclePhase.TEST_COMPILE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class TestCompileMojo extends AbstractMuleMojo {

  @Override
  public void doExecute() {}

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_TEST_COMPILE_PREVIOUS_RUN_PLACEHOLDER";
  }
}

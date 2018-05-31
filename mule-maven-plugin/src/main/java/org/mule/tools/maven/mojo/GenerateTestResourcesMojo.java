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

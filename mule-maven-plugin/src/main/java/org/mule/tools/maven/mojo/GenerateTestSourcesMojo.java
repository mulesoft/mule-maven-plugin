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

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.api.packager.sources.MuleContentGenerator;

/**
 * Build a Mule application archive.
 */
@Mojo(name = "generate-test-sources",
    defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateTestSourcesMojo extends AbstractMuleMojo {

  @Override
  public void doExecute() throws MojoFailureException {
    getLog().debug("Generating test source code...");
    try {
      ((MuleContentGenerator) getContentGenerator()).createTestFolderContent();
    } catch (IllegalArgumentException | IOException e) {
      throw new MojoFailureException("Fail to generate sources", e);
    }
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_GENERATE_TEST_SOURCES_PREVIOUS_RUN_PLACEHOLDER";
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo;


import java.io.IOException;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.mule.tools.api.packager.sources.MuleContentGenerator;


/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
@Mojo(name = "compile",
    defaultPhase = LifecyclePhase.COMPILE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CompileMojo extends AbstractMuleMojo {

  @Component
  private PluginDescriptor descriptor;

  @Override
  public void doExecute() throws MojoFailureException {
    getLog().debug("Generating mule source code...");
    try {
      ((MuleContentGenerator) getContentGenerator()).createMuleSrcFolderContent();
    } catch (IllegalArgumentException | IOException e) {
      throw new MojoFailureException("Fail to compile", e);
    }
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_COMPILE_PREVIOUS_RUN_PLACEHOLDER";
  }

}

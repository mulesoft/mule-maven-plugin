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

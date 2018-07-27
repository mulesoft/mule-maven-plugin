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

import org.mule.tools.api.exception.ValidationException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "verify",
    defaultPhase = LifecyclePhase.VERIFY,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class VerifyMojo extends AbstractMuleMojo {

  @Override
  public void doExecute() throws MojoExecutionException {
    getLog().debug("Verifying packaged files...");
    try {
      if (!skipValidation) {
        getProjectVerifier().verify();
      } else {
        getLog().debug("Skipping verify validation for Mule application");
      }
    } catch (ValidationException e) {
      throw new MojoExecutionException("Verify exception", e);
    }
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_VERIFY_PREVIOUS_RUN_PLACEHOLDER";
  }

}

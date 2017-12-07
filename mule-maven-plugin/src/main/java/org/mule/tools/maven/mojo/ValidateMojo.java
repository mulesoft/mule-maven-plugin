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

import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.VALIDATE;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.project.AbstractProjectValidator;
import org.mule.tools.api.validation.VersionUtils;


/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "validate",
    defaultPhase = LifecyclePhase.VALIDATE,
    requiresDependencyResolution = ResolutionScope.TEST)
public class ValidateMojo extends AbstractMuleMojo {

  private static final String MIN_MAVEN_VERSION = "3.3.3";

  @Override
  public void doExecute() throws MojoExecutionException, MojoFailureException {
    if (!skipValidation) {
      try {
        validateMavenEnvironment();
        getLog().debug("Validating Mule application...");
        AbstractProjectValidator.isPackagingTypeValid(project.getPackaging());
        getProjectValidator().isProjectValid(VALIDATE.id());
      } catch (ValidationException e) {
        throw new MojoExecutionException("Validation exception", e);
      }
    } else {
      getLog().debug("Skipping Validation for Mule application");
    }
  }

  protected void validateMavenEnvironment() throws ValidationException {
    getLog().debug("Validating Maven environment...");

    String mavenVersion = (String) session.getRequest().getSystemProperties().get("maven.version");
    if (!VersionUtils.isVersionGraterOrEquals(mavenVersion, MIN_MAVEN_VERSION)) {
      throw new ValidationException("Your Maven installation version is: " + mavenVersion + " We require at least:"
          + MIN_MAVEN_VERSION);
    }
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_VALIDATE_PREVIOUS_RUN_PLACEHOLDER";
  }

}

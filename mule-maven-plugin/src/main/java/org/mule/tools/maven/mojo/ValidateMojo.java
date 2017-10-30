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

import java.text.MessageFormat;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.api.validation.*;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.model.Deployment;


/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "validate",
    defaultPhase = LifecyclePhase.VALIDATE,
    requiresDependencyResolution = ResolutionScope.TEST)
public class ValidateMojo extends AbstractMuleMojo {

  @Parameter
  protected Deployment deploymentConfiguration;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!skipValidation) {
      long start = System.currentTimeMillis();
      getLog().debug("Validating Mule application...");
      try {
        AbstractProjectValidator.isPackagingTypeValid(project.getPackaging());
        getProjectValidator().isProjectValid();
      } catch (ValidationException e) {
        throw new MojoExecutionException("Validation exception", e);
      }
      getLog().debug(MessageFormat.format("Validation for Mule application done ({0}ms)", System.currentTimeMillis() - start));
    } else {
      getLog().debug("Skipping Validation for Mule application");
    }
  }



  public AbstractProjectValidator getProjectValidator() {
    return ProjectValidatorFactory
        .create(getProjectInformation(), getAetherMavenClient(), sharedLibraries, deploymentConfiguration);
  }
}

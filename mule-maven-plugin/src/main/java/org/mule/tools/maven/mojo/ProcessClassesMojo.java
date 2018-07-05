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
import org.mule.tools.api.exception.ValidationException;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;


/**
 * Post process the generated files from compilation, which in this case will be the mule-artifact.json from the compiled java
 * classes plus any other resource already copied to the output directory.
 */
@Mojo(name = "process-classes",
    defaultPhase = LifecyclePhase.PROCESS_CLASSES,
    requiresDependencyResolution = ResolutionScope.TEST)
public class ProcessClassesMojo extends AbstractMuleMojo {

  @Override
  public void doExecute() throws MojoExecutionException {
    getLog().debug("Generating process-classes code...");
    if (!skipValidation) {
      try {
        getLog().debug("Process classes Mule application...");
        getContentGenerator().copyDescriptorFile();
        getProjectValidator().isProjectValid(VALIDATE.id());
      } catch (ValidationException | IOException e) {
        throw new MojoExecutionException("process-classes exception", e);
      }
    } else {
      getLog().debug("Skipping process-classes  for Mule application");
    }
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_PROCESS_CLASSES_PREVIOUS_RUN_PLACEHOLDER";
  }

}

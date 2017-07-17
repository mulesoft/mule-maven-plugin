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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.maven.dependency.MulePluginsCompatibilityValidator;
import org.mule.tools.maven.dependency.resolver.MulePluginResolver;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.Validator;

/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "validate",
    defaultPhase = LifecyclePhase.VALIDATE,
    requiresDependencyResolution = ResolutionScope.TEST)
public class ValidateMojo extends AbstractMuleMojo {

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!skipValidation) {
      long start = System.currentTimeMillis();
      getLog().debug("Validating Mule application...");
      try {
        getValidator().isProjectValid(project.getPackaging());
      } catch (ValidationException e) {
        throw new MojoExecutionException("Validation exception", e);
      }

      validateMulePluginDependencies();
      validateSharedLibraries();

      getLog().debug(MessageFormat.format("Validation for Mule application done ({0}ms)", System.currentTimeMillis() - start));
    } else {
      getLog().debug("Skipping Validation for Mule application");
    }
  }

  protected void validateSharedLibraries() throws MojoExecutionException {
    if (sharedLibraries != null && sharedLibraries.size() != 0) {
      Set<String> projectDependenciesCoordinates = project.getDependencies().stream()
          .map(dependency -> dependency.getArtifactId() + ":" + dependency.getGroupId()).collect(Collectors.toSet());
      Set<String> sharedLibrariesCoordinates = sharedLibraries.stream()
          .map(dependency -> dependency.getArtifactId() + ":" + dependency.getGroupId()).collect(Collectors.toSet());

      if (!projectDependenciesCoordinates.containsAll(sharedLibrariesCoordinates)) {
        sharedLibrariesCoordinates.removeAll(projectDependenciesCoordinates);
        throw new MojoExecutionException("The mule application does not contain the following shared libraries: "
            + sharedLibrariesCoordinates.toString());
      }
    }
  }


  protected void validateMulePluginDependencies() throws MojoExecutionException {
    getMulePluginsCompatibilityValidator().validate(getResolver().resolveMulePlugins(project));
  }

  protected Validator getValidator() {
    return new Validator(projectBaseFolder.toPath());
  }

  protected MulePluginResolver getResolver() {
    return new MulePluginResolver(getLog(), session, projectBuilder, repositorySystem, localRepository,
                                  remoteArtifactRepositories);
  }

  protected MulePluginsCompatibilityValidator getMulePluginsCompatibilityValidator() {
    return new MulePluginsCompatibilityValidator();
  }
}

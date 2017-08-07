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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.validation.MulePluginsCompatibilityValidator;
import org.mule.tools.maven.utils.MavenProjectBuilder;
import org.mule.tools.maven.dependency.MulePluginResolver;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.Validator;

/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "validate",
    defaultPhase = LifecyclePhase.VALIDATE,
    requiresDependencyResolution = ResolutionScope.TEST)
public class ValidateMojo extends AbstractMuleMojo {

  private Validator validator;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!skipValidation) {
      long start = System.currentTimeMillis();
      getLog().debug("Validating Mule application...");
      try {
        getValidator().isProjectValid(project.getPackaging());
        getMulePluginsCompatibilityValidator().validate(toArtifactCoordinates(getResolver().resolveMulePlugins(project)));
        getValidator().validateSharedLibraries(sharedLibraries, toArtifactCoordinates(project.getDependencies()));
      } catch (ValidationException e) {
        throw new MojoExecutionException("Validation exception", e);
      }
      getLog().debug(MessageFormat.format("Validation for Mule application done ({0}ms)", System.currentTimeMillis() - start));
    } else {
      getLog().debug("Skipping Validation for Mule application");
    }
  }

  private List<ArtifactCoordinates> toArtifactCoordinates(List<Dependency> dependencies) {
    return dependencies
        .stream().map(d -> new ArtifactCoordinates(d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getType(),
                                                   d.getClassifier()))
        .collect(Collectors.toList());
  }

  protected Validator getValidator() {
    if (validator == null) {
      validator = new Validator(projectBaseFolder.toPath());
    }
    return validator;
  }

  protected MulePluginResolver getResolver() {
    MavenProjectBuilder builder = new MavenProjectBuilder(getLog(), session, projectBuilder, repositorySystem, localRepository,
                                                          remoteArtifactRepositories);
    return new MulePluginResolver(builder);
  }

  protected MulePluginsCompatibilityValidator getMulePluginsCompatibilityValidator() {
    return new MulePluginsCompatibilityValidator();
  }
}

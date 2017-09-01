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

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.validation.*;
import org.mule.tools.maven.utils.DependencyProject;
import org.mule.tools.maven.utils.MavenProjectBuilder;
import org.mule.tools.api.exception.ValidationException;

import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN_BUNDLE;

/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "validate",
    defaultPhase = LifecyclePhase.VALIDATE,
    requiresDependencyResolution = ResolutionScope.TEST)
public class ValidateMojo extends AbstractMuleMojo {

  private AbstractProjectValidator validator;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!skipValidation) {
      long start = System.currentTimeMillis();
      getLog().debug("Validating Mule application...");
      try {
        AbstractProjectValidator.isPackagingTypeValid(project.getPackaging());
        getValidator().isProjectValid();
      } catch (ValidationException e) {
        throw new MojoExecutionException("Validation exception", e);
      }
      getLog().debug(MessageFormat.format("Validation for Mule application done ({0}ms)", System.currentTimeMillis() - start));
    } else {
      getLog().debug("Skipping Validation for Mule application");
    }
  }

  protected AbstractProjectValidator getValidator() throws ValidationException {
    Path projectBaseDir = project.getBasedir().toPath();
    String projectPackagingType = project.getPackaging();
    List<ArtifactCoordinates> projectDependencies = toArtifactCoordinates(project.getDependencies());
    List<ArtifactCoordinates> resolvedMulePlugins = getResolver().resolveMulePlugins(new DependencyProject(project));

    if (PackagingType.fromString(projectPackagingType).equals(MULE_DOMAIN_BUNDLE)) {
      return new DomainBundleProjectValidator(projectBaseDir, projectDependencies, resolvedMulePlugins, getAetherMavenClient());
    }
    return new MuleProjectValidator(projectBaseDir, projectPackagingType, projectDependencies, resolvedMulePlugins,
                                    sharedLibraries);
  }

  protected MulePluginResolver getResolver() {
    MavenProjectBuilder builder = new MavenProjectBuilder(getLog(), session, projectBuilder, repositorySystem, localRepository,
                                                          remoteArtifactRepositories);
    return new MulePluginResolver(builder);
  }

}

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

import static java.util.Arrays.asList;
import static org.mule.tools.api.validation.AllowedDependencyValidator.areDependenciesAllowed;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.validation.DependencyValidator;
import org.mule.tools.api.validation.TestScopeDependencyValidator;
import org.mule.tools.api.validation.VersionUtils;
import org.mule.tools.api.validation.project.AbstractProjectValidator;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;


/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "validate",
    defaultPhase = LifecyclePhase.VALIDATE,
    requiresDependencyResolution = ResolutionScope.TEST)
public class ValidateMojo extends AbstractMuleMojo {

  private static final String MIN_MAVEN_VERSION = "3.3.3";
  private static final DependencyValidator TEST_SCOPE_DEPENDENCY_VALIDATOR = new TestScopeDependencyValidator(
                                                                                                              asList(
                                                                                                                     new TestScopeDependencyValidator.Dependency("com.mulesoft.munit",
                                                                                                                                                                 "munit-runner"),
                                                                                                                     new TestScopeDependencyValidator.Dependency("com.mulesoft.munit",
                                                                                                                                                                 "munit-tools")));

  @Override
  public void doExecute() throws MojoExecutionException {
    if (!skipValidation) {
      try {
        validateMavenEnvironment();

        getLog().debug("Validating Mule application...");

        AbstractProjectValidator.isPackagingTypeValid(project.getPackaging());

        validateNotAllowedDependencies();
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
    if (!VersionUtils.isVersionGreaterOrEquals(mavenVersion, MIN_MAVEN_VERSION)) {
      throw new ValidationException("Your Maven installation version is: " + mavenVersion + " We require at least:"
          + MIN_MAVEN_VERSION);
    }
  }

  protected void validateNotAllowedDependencies() throws ValidationException {
    List<ArtifactCoordinates> dependencies =
        project.getDependencies().stream().map(d -> buildArtifactCoordinates(d)).collect(Collectors.toList());
    if (!project.getPackaging().equals(PackagingType.MULE_DOMAIN_BUNDLE.toString())) {
      areDependenciesAllowed(dependencies);
    }
    TEST_SCOPE_DEPENDENCY_VALIDATOR.areDependenciesValid(dependencies);
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_VALIDATE_PREVIOUS_RUN_PLACEHOLDER";
  }

  protected ArtifactCoordinates buildArtifactCoordinates(Dependency dependency) {
    ArtifactCoordinates artifactCoordinates =
        new ArtifactCoordinates(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    artifactCoordinates.setType(dependency.getType());
    artifactCoordinates.setScope(dependency.getScope());
    artifactCoordinates.setClassifier(dependency.getClassifier());

    return artifactCoordinates;
  }

}

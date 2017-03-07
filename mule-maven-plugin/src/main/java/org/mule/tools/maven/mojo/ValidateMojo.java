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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.mule.tools.maven.dependency.MulePluginsCompatibilityValidator;
import org.mule.tools.maven.dependency.resolver.MulePluginResolver;
import org.mule.tools.maven.mojo.model.PackagingType;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mule.tools.artifact.archiver.api.PackagerFiles.MULE_APPLICATION_JSON;
import static org.mule.tools.artifact.archiver.api.PackagerFiles.MULE_POLICY_JSON;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.MULE;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.POLICY;

/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "validate",
    defaultPhase = LifecyclePhase.VALIDATE,
    requiresDependencyResolution = ResolutionScope.TEST)
public class ValidateMojo extends AbstractMuleMojo {

  protected MulePluginResolver resolver;
  protected MulePluginsCompatibilityValidator validator;

  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().debug("Validating Mule application...");

    validateMandatoryFolders();
    validateMandatoryDescriptors();
    validateMulePluginDependencies();
    validateSharedLibraries();

    getLog().debug("Validating Mule application done");
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

  private void validateMandatoryFolders() throws MojoExecutionException {
    if (!getSourceFolder().exists()) {
      String message = String.format("Invalid Mule project. Missing src/main/"
          + (PackagingType.MULE_POLICY.equals(project.getPackaging()) ? POLICY : MULE) + " folder. This folder is mandatory");
      throw new MojoExecutionException(message);
    }
  }

  private void validateMandatoryDescriptors() throws MojoExecutionException {
    isFilePresent("Invalid Mule project. Missing %s file, it must be present in the root of application",
                  (PackagingType.MULE_POLICY.equals(project.getPackaging()) ? MULE_POLICY_JSON : MULE_APPLICATION_JSON));
  }

  private void isFilePresent(String message, String... fileName) throws MojoExecutionException {
    List<File> files = Arrays.stream(fileName).map(name -> Paths.get(projectBaseFolder.toString(), name).toFile())
        .collect(Collectors.toList());
    if (files.stream().allMatch(file -> !file.exists())) {
      throw new MojoExecutionException(String.format(message, fileName));
    }
  }

  private void validateMulePluginDependencies() throws MojoExecutionException {
    initializeResolver();
    initializeValidator();
    validator.validate(resolver.resolveMulePlugins(project));
  }

  protected void initializeResolver() {
    resolver = new MulePluginResolver(getLog(), session, projectBuilder, repositorySystem, localRepository,
                                      remoteArtifactRepositories);
  }

  protected void initializeValidator() {
    validator = new MulePluginsCompatibilityValidator();
  }
}

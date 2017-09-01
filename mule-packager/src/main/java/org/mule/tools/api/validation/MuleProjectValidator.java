/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.exception.ValidationException;

import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;

/**
 * Ensures the project is valid
 */
public class MuleProjectValidator extends AbstractProjectValidator {


  private final List<SharedLibraryDependency> sharedLibraries;

  public MuleProjectValidator(Path projectBaseDir, String packagingType,
                              List<ArtifactCoordinates> projectDependencies, List<ArtifactCoordinates> resolvedMulePlugins,
                              List<SharedLibraryDependency> sharedLibraries) {
    super(projectBaseDir, packagingType, projectDependencies, resolvedMulePlugins);
    this.sharedLibraries = sharedLibraries;
  }

  @Override
  protected void additionalValidation() throws ValidationException {
    isProjectStructureValid(packagingType);
    isDescriptorFilePresent();
    validateSharedLibraries(sharedLibraries, projectDependencies);
  }

  /**
   * It validates the project folder structure is valid
   * 
   * @return true if the project's structure is valid
   * @throws ValidationException if the project's structure is invalid
   */
  public Boolean isProjectStructureValid(String packagingType) throws ValidationException {
    File mainSrcApplication = mainSrcApplication(packagingType);
    if (!mainSrcApplication.exists()) {
      throw new ValidationException("The folder " + mainSrcApplication.getAbsolutePath() + " is mandatory");
    }
    return true;
  }

  /**
   * It validates that the mandatory descriptor files are present
   * 
   * @return true if the project's descriptor files are preset
   * @throws ValidationException if the project's descriptor files are missing
   */
  public Boolean isDescriptorFilePresent() throws ValidationException {
    String errorMessage = "Invalid Mule project. Missing %s file, it must be present in the root of application";
    if (!projectBaseDir.resolve(MULE_ARTIFACT_JSON).toFile().exists()) {
      throw new ValidationException(String.format(errorMessage, MULE_ARTIFACT_JSON));
    }
    return true;
  }

  /**
   * It validates if every shared library is present in the project dependencies.
   *
   * @return true if every shared library is declared in the project dependencies
   * @throws ValidationException if at least one shared library is not defined in the project dependencies
   */
  public void validateSharedLibraries(List<SharedLibraryDependency> sharedLibraries,
                                      List<ArtifactCoordinates> projectDependencies)
      throws ValidationException {
    if (sharedLibraries != null && sharedLibraries.size() != 0) {
      Set<String> projectDependenciesCoordinates = projectDependencies.stream()
          .map(coordinate -> coordinate.getArtifactId() + ":" + coordinate.getGroupId()).collect(Collectors.toSet());
      Set<String> sharedLibrariesCoordinates = sharedLibraries.stream()
          .map(sharedLibrary -> sharedLibrary.getArtifactId() + ":" + sharedLibrary.getGroupId()).collect(Collectors.toSet());

      if (!projectDependenciesCoordinates.containsAll(sharedLibrariesCoordinates)) {
        sharedLibrariesCoordinates.removeAll(projectDependenciesCoordinates);
        throw new ValidationException("The mule application does not contain the following shared libraries: "
            + sharedLibrariesCoordinates.toString());
      }
    }
  }

  private File mainSrcApplication(String packagingType) throws ValidationException {
    return PackagingType.fromString(packagingType).getSourceFolderLocation(projectBaseDir).toFile();
  }

}

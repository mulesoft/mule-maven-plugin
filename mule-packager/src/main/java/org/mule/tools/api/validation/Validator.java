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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.exception.ValidationException;

import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;

/**
 * Ensures the project is valid
 */
public class Validator {

  private Path projectBaseDir;

  public Validator(Path projectBaseDir) {
    this.projectBaseDir = projectBaseDir;
  }

  /**
   * Ensure a project contained in the projectBaseDir is valid based on its packagin type.
   *
   * @param packagingType defines the package type of the project to validate
   * @return true if the project is valid
   * @throws ValidationException if the project is invalid
   */
  public Boolean isProjectValid(String packagingType) throws ValidationException {
    isPackagingTypeValid(packagingType);
    isProjectStructureValid(packagingType);
    isDescriptorFilePresent();

    return true;
  }

  /**
   * It validates that the provided packaging types is a valid one
   *
   * @param packagingType defines the package type of the project to validate
   * @return true if the project's packaging type is valid
   * @throws ValidationException if the packaging type is unknown
   */
  public Boolean isPackagingTypeValid(String packagingType) throws ValidationException {
    try {
      PackagingType.fromString(packagingType);
    } catch (IllegalArgumentException e) {
      List<String> packagingTypeNames = Arrays.stream(PackagingType.values()).map(type -> type.toString())
          .collect(Collectors.toList());
      throw new ValidationException("Unknown packaging type " + packagingType
          + ". Please specify a valid mule packaging type: " + String.join(", ", packagingTypeNames));
    }
    return true;
  }

  /**
   * It validates the project folder structure is valid
   * 
   * @param packagingType defines the package type of the project to validate
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

  private File mainSrcApplication(String packagingType) throws ValidationException {
    return PackagingType.fromString(packagingType).getSourceFolderLocation(projectBaseDir).toFile();
  }

}

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

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

/**
 * Validates if the project has an existent packaging type, the compatibility of mule plugins that are dependencies of this
 * project and performs any additional validation defined by its subclasses.
 */
public abstract class AbstractProjectValidator {

  protected final ProjectInformation projectInformation;
  protected final boolean strictCheck;


  public AbstractProjectValidator(ProjectInformation projectInformation, boolean strictCheck) {
    this.projectInformation = projectInformation;
    this.strictCheck = strictCheck;
  }

  /**
   * Ensure a project contained in the projectBaseDir is valid based on its packaging type.
   *
   * @return true if the project is valid
   * @throws ValidationException if the project is invalid
   */
  public Boolean isProjectValid(String goal) throws ValidationException {
    if (StringUtils.equals("validate", goal)) {
      checkState(projectInformation.getPackaging() != null, "Packaging type should not be null");
      isProjectVersionValid(projectInformation.getVersion());
      isPackagingTypeValid(projectInformation.getPackaging());
      additionalValidation();
      if (strictCheck) {
        isProjectValid("deploy");
      }
    } else if (StringUtils.equals("deploy", goal)) {
      isDeploymentValid();
    }
    return true;
  }

  protected static void isProjectVersionValid(String version) throws ValidationException {
    if (!VersionUtils.isVersionValid(version)) {
      throw new ValidationException("Version " + version + " does not comply with semantic versioning specification");
    }
  }

  protected abstract void additionalValidation() throws ValidationException;

  protected abstract void isDeploymentValid() throws ValidationException;

  /**
   * It validates that the provided packaging types is a valid one
   *
   * @param packagingType defines the package type of the project to validate
   * @return true if the project's packaging type is valid
   * @throws ValidationException if the packaging type is unknown
   */
  public static Boolean isPackagingTypeValid(String packagingType) throws ValidationException {
    try {
      PackagingType.fromString(packagingType);
    } catch (IllegalArgumentException e) {
      List<String> packagingTypeNames = Arrays.stream(PackagingType.values()).map(PackagingType::toString)
          .collect(Collectors.toList());
      throw new ValidationException(packagingType == null ? e.getMessage() : "Unknown packaging type " + packagingType
          + ". Please specify a valid mule packaging type: " + String.join(", ", packagingTypeNames));
    }
    return true;
  }
}

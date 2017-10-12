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
import org.mule.tools.api.util.Project;

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
  protected final Project dependencyProject;
  private final MulePluginResolver resolver;
  protected final MulePluginsCompatibilityValidator mulePluginsCompatibilityValidator = new MulePluginsCompatibilityValidator();

  public AbstractProjectValidator(ProjectInformation projectInformation, Project dependencyProject,
                                  MulePluginResolver resolver) {
    this.projectInformation = projectInformation;
    this.dependencyProject = dependencyProject;
    this.resolver = resolver;
  }

  /**
   * Ensure a project contained in the projectBaseDir is valid based on its packaging type.
   *
   * @return true if the project is valid
   * @throws ValidationException if the project is invalid
   */
  public Boolean isProjectValid() throws ValidationException {
    checkState(projectInformation.getPackaging() != null, "Packaging type should not be null");
    isProjectVersionValid(projectInformation.getVersion());
    isPackagingTypeValid(projectInformation.getPackaging());
    mulePluginsCompatibilityValidator.validate(resolver.resolveMulePlugins(dependencyProject));
    additionalValidation();
    return true;
  }

  protected static void isProjectVersionValid(String version) throws ValidationException {
    String prefixPattern = "^(0|([1-9]\\d*))\\.(0|([1-9]\\d*))\\.(0|([1-9]\\d*))$"; // X.Y.Z with X, Y, Z integers with no leading
                                                                                    // zeroes
    String suffixPattern = "^([a-zA-Z0-9]|\\.|-)*$"; // contains only alphanumeric characters, dots (.) or dashes (-)
    int separatorIndex = getSeparatorIndex(version);
    String prefix = separatorIndex == -1 ? version : version.substring(0, separatorIndex);
    String suffix = separatorIndex == -1 ? StringUtils.EMPTY : version.substring(separatorIndex + 1);
    if (!prefix.matches(prefixPattern) || !suffix.matches(suffixPattern) || separatorIndex == version.length() - 1) {
      throw new ValidationException("Project version does not comply with semantic versioning specification");
    }
  }

  protected static int getSeparatorIndex(String version) {
    int plusPosition = version.indexOf('+');
    int minusPosition = version.indexOf('-');
    if (plusPosition == -1 || minusPosition == -1) {
      return Math.max(plusPosition, minusPosition);
    }
    return Math.min(plusPosition, minusPosition);
  }

  protected abstract void additionalValidation() throws ValidationException;

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
      List<String> packagingTypeNames = Arrays.stream(PackagingType.values()).map(type -> type.toString())
          .collect(Collectors.toList());
      throw new ValidationException(packagingType == null ? e.getMessage() : "Unknown packaging type " + packagingType
          + ". Please specify a valid mule packaging type: " + String.join(", ", packagingTypeNames));
    }
    return true;
  }
}

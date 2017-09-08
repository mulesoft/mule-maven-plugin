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

import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.util.Project;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

/**
 * Validates if the project has an existent packaging type, the compatibility of mule plugins that are dependencies of this
 * project and performs any additional validation defined by its subclasses.
 */
public abstract class AbstractProjectValidator {

  protected final Path projectBaseDir;
  protected final String packagingType;
  protected final Project dependencyProject;
  private final MulePluginResolver resolver;
  protected final MulePluginsCompatibilityValidator mulePluginsCompatibilityValidator = new MulePluginsCompatibilityValidator();

  public AbstractProjectValidator(Path projectBaseDir, String packagingType, Project dependencyProject,
                                  MulePluginResolver resolver) {
    this.projectBaseDir = projectBaseDir;
    this.packagingType = packagingType;
    this.dependencyProject = dependencyProject;
    this.resolver = resolver;
  }

  /**
   * Ensure a project contained in the projectBaseDir is valid based on its packagin type.
   *
   * @return true if the project is valid
   * @throws ValidationException if the project is invalid
   */
  public Boolean isProjectValid() throws ValidationException {
    checkState(packagingType != null, "Packaging type should not be null");
    isPackagingTypeValid(packagingType);
    mulePluginsCompatibilityValidator.validate(resolver.resolveMulePlugins(dependencyProject));
    additionalValidation();
    return true;
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
      throw new ValidationException("Unknown packaging type " + packagingType
          + ". Please specify a valid mule packaging type: " + String.join(", ", packagingTypeNames));
    }
    return true;
  }
}

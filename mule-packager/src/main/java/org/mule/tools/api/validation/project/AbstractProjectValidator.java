/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.project;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.StringUtils.join;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.Classifier;
import org.mule.tools.api.packager.packaging.PackagingType;

/**
 * Validates if the project has an existent packaging type, the compatibility of mule plugins that are dependencies of this
 * project and performs any additional validation defined by its subclasses.
 */
public abstract class AbstractProjectValidator {

  public static final String VALIDATE_GOAL = "validate";

  protected final ProjectInformation projectInformation;

  public AbstractProjectValidator(ProjectInformation projectInformation) {
    this.projectInformation = projectInformation;
    checkState(projectInformation.getPackaging() != null, "Packaging type should not be null");
  }

  protected abstract void additionalValidation() throws ValidationException;

  /**
   * Ensure a project contained in the projectBaseDir is valid based on its packaging type.
   *
   * @return true if the project is valid
   * @throws ValidationException if the project is invalid
   */
  public Boolean isProjectValid(String goal) throws ValidationException {
    if (StringUtils.equals(VALIDATE_GOAL, goal)) {
      isPackagingTypeValid(projectInformation.getPackaging());
      additionalValidation();
    }
    return true;
  }

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
      List<String> packagingTypeNames = newArrayList();
      for (int i = 0; i < PackagingType.values().length; ++i) {
        packagingTypeNames.add(PackagingType.values()[i].toString());
      }
      throw new ValidationException(packagingType == null ? e.getMessage()
          : "Unknown packaging type " + packagingType
              + ". Please specify a valid mule packaging type: " + join(", ", packagingTypeNames));
    }
    return true;
  }

  /**
   * It validates that the provided packaging types is a valid one
   *
   * @param classifier defines the classifier of the project to validate
   * @return true if the project's packaging type is valid
   * @throws ValidationException if the packaging type is unknown
   */
  public static Boolean isClassifierValid(String classifier) throws ValidationException {
    Set<String> allClassifiers = newHashSet();
    for (int i = 0; i < Classifier.values().length; ++i) {
      allClassifiers.add(Classifier.values()[i].toString());
    }
    if (classifier == null || !allClassifiers.contains(classifier)) {
      List<String> classifierNames = newArrayList(allClassifiers);
      throw new ValidationException("Unknown classifier type " + classifier
          + ". Please specify a valid mule classifier type: " + join(", ", classifierNames));
    }
    return true;
  }
}

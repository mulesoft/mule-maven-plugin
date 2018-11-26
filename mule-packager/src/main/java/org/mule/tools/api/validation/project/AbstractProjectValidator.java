/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation.project;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.Classifier;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.validation.VersionUtils;

/**
 * Validates if the project has an existent packaging type, the compatibility of mule plugins that are dependencies of this
 * project and performs any additional validation defined by its subclasses.
 */
public abstract class AbstractProjectValidator {

  public static final String DEPLOY_GOAL = "deploy";
  public static final String VALIDATE_GOAL = "validate";

  protected final boolean strictCheck;
  protected final ProjectInformation projectInformation;
  private final boolean disableSemver;

  public AbstractProjectValidator(ProjectInformation projectInformation, boolean strictCheck) {
    this(projectInformation, new ProjectRequirement.ProjectRequirementBuilder().withStrictCheck(strictCheck).build());
  }

  public AbstractProjectValidator(ProjectInformation projectInformation, ProjectRequirement requirement) {
    this.projectInformation = projectInformation;
    this.strictCheck = requirement.isStrictCheck();
    this.disableSemver = requirement.disableSemver();
    checkState(projectInformation.getPackaging() != null, "Packaging type should not be null");
  }

  protected abstract void additionalValidation() throws ValidationException;

  // TODO THIS SHOULD NOT BE PART OF THIS CLASS
  protected abstract void isDeploymentValid() throws ValidationException;

  /**
   * Ensure a project contained in the projectBaseDir is valid based on its packaging type.
   *
   * @return true if the project is valid
   * @throws ValidationException if the project is invalid
   */
  public Boolean isProjectValid(String goal) throws ValidationException {
    if (StringUtils.equals(VALIDATE_GOAL, goal)) {
      if (!disableSemver) {
        isProjectVersionValid(projectInformation.getVersion());
      }
      isPackagingTypeValid(projectInformation.getPackaging());
      additionalValidation();
      if (strictCheck) {
        isDeploymentValid();
      }
    }

    if (StringUtils.equals(DEPLOY_GOAL, goal)) {
      isDeploymentValid();
    }

    return true;
  }

  private static void isProjectVersionValid(String version) throws ValidationException {
    if (!VersionUtils.isVersionValid(version)) {
      throw new ValidationException("Version " + version + " does not comply with semantic versioning specification");
    }
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
      List<String> packagingTypeNames = Arrays.stream(PackagingType.values()).map(PackagingType::toString)
          .collect(Collectors.toList());
      throw new ValidationException(packagingType == null ? e.getMessage()
          : "Unknown packaging type " + packagingType
              + ". Please specify a valid mule packaging type: " + String.join(", ", packagingTypeNames));
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
    Set<String> allPossibleClassifiers = generateAllValidClassifiers();
    if (classifier == null || !allPossibleClassifiers.contains(classifier)) {
      List<String> classifierNames = allPossibleClassifiers.stream().collect(Collectors.toList());
      throw new ValidationException("Unknown classifier type " + classifier
          + ". Please specify a valid mule classifier type: " + String
              .join(", ", classifierNames));
    }
    return true;
  }

  private static Set<String> generateAllValidClassifiers() {
    Set<String> classifierPrefixes = Arrays.stream(Classifier.values())
        .filter(c -> !c.equals(Classifier.LIGHT_PACKAGE))
        .filter(c -> !c.equals(Classifier.TEST_JAR))
        .map(Classifier::toString).collect(Collectors.toSet());

    Set<String> allPossibleClassifiers = new HashSet<>(classifierPrefixes);

    for (String prefix : classifierPrefixes) {
      allPossibleClassifiers.add(prefix + "-" + Classifier.LIGHT_PACKAGE.toString());
    }

    for (String prefix : new ArrayList<>(allPossibleClassifiers)) {
      allPossibleClassifiers.add(prefix + "-" + Classifier.TEST_JAR.toString());
    }
    return allPossibleClassifiers;
  }
}

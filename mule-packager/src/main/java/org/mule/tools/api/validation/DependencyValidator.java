/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

import java.util.List;

/**
 * Should implement logic for validating a {@link org.mule.maven.pom.parser.api.model.BundleDependency}
 */
public interface DependencyValidator {

  /**
   * Validates that the {@link ArtifactCoordinates} are correct.
   * @param dependencies the dependencies to be validated
   * @return true if the dependencies are valid, false otherwise
   * @throws ValidationException if there is a problem with the {@link ArtifactCoordinates}
   * and validation should be stopped.
   */
  boolean areDependenciesValid(List<ArtifactCoordinates> dependencies) throws ValidationException;

}

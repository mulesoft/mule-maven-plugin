/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.validation;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

import java.util.List;

/**
 * Should implement logic for validating a {@link BundleDependency}
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

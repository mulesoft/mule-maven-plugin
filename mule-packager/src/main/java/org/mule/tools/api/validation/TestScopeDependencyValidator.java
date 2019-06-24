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

import static java.lang.System.lineSeparator;
import static org.eclipse.aether.util.artifact.JavaScopes.TEST;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

public class TestScopeDependencyValidator implements DependencyValidator {

  private List<Dependency> expectedDependencies;

  public TestScopeDependencyValidator(List<Dependency> expectedDependencies) {
    this.expectedDependencies = expectedDependencies;
  }

  @Override
  public boolean areDependenciesValid(List<ArtifactCoordinates> dependencies) throws ValidationException {
    List<String> invalidDependenciesExceptionsMessages = new ArrayList<>();
    for (ArtifactCoordinates dependency : dependencies) {
      try {
        isValid(dependency);
      } catch (ValidationException e) {
        invalidDependenciesExceptionsMessages.add(e.getMessage());
      }
    }
    if (!invalidDependenciesExceptionsMessages.isEmpty()) {
      StringBuilder invalidDependenciesMessage = new StringBuilder();
      invalidDependenciesExceptionsMessages.forEach(d -> invalidDependenciesMessage.append(d).append(lineSeparator()));
      throw new ValidationException("The following dependencies are not allowed unless their scope is [" + TEST + "]: "
          + lineSeparator() + invalidDependenciesMessage.toString());

    }
    return true;
  }

  private boolean isValid(ArtifactCoordinates dependency) throws ValidationException {
    boolean isDependencyPresent =
        expectedDependencies.stream().anyMatch(d -> d.artifactId.equals(dependency.getArtifactId())
            && d.groupId.equals(dependency.getGroupId()));
    if (isDependencyPresent) {
      if (!TEST.equals(dependency.getScope())) {
        throw new ValidationException("Dependency: " + dependency + " should have scope \'test\', found \'"
            + dependency.getScope() + "\'");
      }
    }
    return true;
  }

  public static final class Dependency {

    private final String groupId;
    private final String artifactId;

    public Dependency(String groupId, String artifactId) {
      this.groupId = groupId;
      this.artifactId = artifactId;
    }

  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

import java.util.List;

public class TestScopeDependencyValidatorTest {

  private static final String GROUP_ID = "org.mule.test";
  private static final String SECOND_GROUP_ID = "org.mule.testgroup";
  private static final String ARTIFACT_ID = "artifact";

  private DependencyValidator testScopeDependencyValidator;

  @BeforeEach
  public void setUp() {
    this.testScopeDependencyValidator =
        new TestScopeDependencyValidator(singletonList(new TestScopeDependencyValidator.Dependency(GROUP_ID, ARTIFACT_ID)),
                                         singletonList(SECOND_GROUP_ID));
  }

  @Test
  public void testExceptionWhenInvalidDependencyValidatingMultiple() {
    assertThatThrownBy(() -> {
      List<ArtifactCoordinates> dependencies = singletonList(createCoordinates(GROUP_ID, ARTIFACT_ID, "compile"));
      testScopeDependencyValidator.areDependenciesValid(dependencies);
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void testExceptionWhenInvalidGroupValidatingMultiple() {
    assertThatThrownBy(() -> {
      List<ArtifactCoordinates> dependencies = singletonList(createCoordinates(SECOND_GROUP_ID, ARTIFACT_ID, "compile"));
      testScopeDependencyValidator.areDependenciesValid(dependencies);
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void validatorDoesNotFailIfScopeIsTests() throws Exception {
    List<ArtifactCoordinates> dependencies = singletonList(createCoordinates(GROUP_ID, ARTIFACT_ID, "test"));
    assertThat(testScopeDependencyValidator.areDependenciesValid(dependencies)).isTrue();
  }


  private ArtifactCoordinates createCoordinates(String groupId, String artifactId, String scope) {
    return new ArtifactCoordinates(groupId, artifactId, "0.0.0", "jar", "", scope);
  }

}

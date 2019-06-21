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

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.rules.ExpectedException.none;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestScopeDependencyValidatorTest {

  private static final String GROUP_ID = "org.mule.test";
  private static final String ARTIFACT_ID = "artifact";
  private static final DependencyValidator VALIDATOR = new TestScopeDependencyValidator(
                                                                                        singletonList(
                                                                                                      new TestScopeDependencyValidator.Dependency(GROUP_ID,
                                                                                                                                                  ARTIFACT_ID)));

  @Rule
  public ExpectedException expectedException = none();

  @Test
  public void testExceptionWhenInvalidDependency() throws Exception {
    ArtifactCoordinates dependency = createCoordinates(GROUP_ID, ARTIFACT_ID, "compile");
    expectedException.expect(ValidationException.class);
    VALIDATOR.isValid(dependency);
  }

  @Test
  public void testExceptionWhenInvalidDependencyValidatingMultiple() throws Exception {
    List<ArtifactCoordinates> dependencies = singletonList(createCoordinates(GROUP_ID, ARTIFACT_ID, "compile"));
    expectedException.expect(ValidationException.class);
    VALIDATOR.areDependenciesValid(dependencies);
  }

  @Test
  public void validatorDoesNotFailIfScopeIsTests() throws Exception {
    List<ArtifactCoordinates> dependencies = singletonList(createCoordinates(GROUP_ID, ARTIFACT_ID, "test"));
    assertThat(VALIDATOR.isValid(dependencies.get(0)), is(true));
    assertThat(VALIDATOR.areDependenciesValid(dependencies), is(true));
  }


  private ArtifactCoordinates createCoordinates(String groupId, String artifactId, String scope) {
    return new ArtifactCoordinates(groupId, artifactId, "0.0.0", "jar", "", scope);
  }

}

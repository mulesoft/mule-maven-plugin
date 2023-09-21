/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mule.tools.api.packager.packaging.Classifier.LIGHT_PACKAGE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_EXAMPLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_TEMPLATE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN_BUNDLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;
import static org.mule.tools.api.packager.packaging.Classifier.TEST_JAR;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mule.tools.api.muleclassloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

public class AllowedDependencyValidatorTest {

  public static final String COMPILE = "compile";
  public static final String PROVIDED = "provided";

  public static Stream<Arguments> data() {
    return Stream.of(
                     Arguments.of("mule-service", COMPILE, FALSE),
                     Arguments.of("mule-service", PROVIDED, TRUE),
                     Arguments.of("mule-server-plugin", COMPILE, FALSE),
                     Arguments.of("mule-server-plugin", PROVIDED, TRUE),

                     Arguments.of(MULE_DOMAIN_BUNDLE.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_DOMAIN_BUNDLE.toString(), PROVIDED, FALSE),

                     Arguments.of(MULE_DOMAIN.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_DOMAIN.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_DOMAIN.toString() + "-" + LIGHT_PACKAGE.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_DOMAIN.toString() + "-" + LIGHT_PACKAGE.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_DOMAIN.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_DOMAIN.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_DOMAIN.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), COMPILE,
                                  FALSE),
                     Arguments.of(MULE_DOMAIN.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), PROVIDED,
                                  TRUE),


                     Arguments.of(MULE_POLICY.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_POLICY.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_POLICY.toString() + "-" + LIGHT_PACKAGE.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_POLICY.toString() + "-" + LIGHT_PACKAGE.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_POLICY.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_POLICY.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_POLICY.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), COMPILE,
                                  FALSE),
                     Arguments.of(MULE_POLICY.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), PROVIDED,
                                  TRUE),

                     Arguments.of(MULE_APPLICATION.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_APPLICATION.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_APPLICATION.toString() + "-" + LIGHT_PACKAGE.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_APPLICATION.toString() + "-" + LIGHT_PACKAGE.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_APPLICATION.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_APPLICATION.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_APPLICATION.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(),
                                  COMPILE, FALSE),
                     Arguments.of(MULE_APPLICATION.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(),
                                  PROVIDED, TRUE),


                     Arguments.of(MULE_APPLICATION_EXAMPLE.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_APPLICATION_EXAMPLE.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_APPLICATION_EXAMPLE.toString() + "-" + LIGHT_PACKAGE.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_APPLICATION_EXAMPLE.toString() + "-" + LIGHT_PACKAGE.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_APPLICATION_EXAMPLE.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_APPLICATION_EXAMPLE.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE),

                     Arguments
                         .of(MULE_APPLICATION_EXAMPLE.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(),
                             COMPILE, FALSE),
                     Arguments
                         .of(MULE_APPLICATION_EXAMPLE.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(),
                             PROVIDED, TRUE),


                     Arguments.of(MULE_APPLICATION_TEMPLATE.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_APPLICATION_TEMPLATE.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_APPLICATION_TEMPLATE.toString() + "-" + LIGHT_PACKAGE.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_APPLICATION_TEMPLATE.toString() + "-" + LIGHT_PACKAGE.toString(), PROVIDED, TRUE),

                     Arguments.of(MULE_APPLICATION_TEMPLATE.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE),
                     Arguments.of(MULE_APPLICATION_TEMPLATE.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE),

                     Arguments
                         .of(MULE_APPLICATION_TEMPLATE.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(),
                             COMPILE, FALSE),
                     Arguments
                         .of(MULE_APPLICATION_TEMPLATE.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(),
                             PROVIDED, TRUE));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void areDependenciesAllowed(String classifier, String scope, Boolean isAllowed) {
    ArtifactCoordinates artifactCoordinates;
    artifactCoordinates = buildFakeArtifactCoordinates();
    artifactCoordinates.setScope(scope);
    artifactCoordinates.setClassifier(classifier);
    try {
      AllowedDependencyValidator.areDependenciesAllowed(asList(artifactCoordinates));
    } catch (ValidationException e) {
      if (isAllowed) {
        fail("The artifact " + artifactCoordinates.toString() + " should not pass the validation");
      }
      return;
    }

    if (!isAllowed) {
      fail("The artifact " + artifactCoordinates.toString() + " should not pass the validation");
    }
  }

  private ArtifactCoordinates buildFakeArtifactCoordinates() {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates("fake.group", "fake-artifact", "0.0.0");
    artifactCoordinates.setScope(COMPILE);
    return artifactCoordinates;
  }



}

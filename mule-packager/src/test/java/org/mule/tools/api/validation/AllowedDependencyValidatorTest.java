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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.mule.tools.api.packager.packaging.Classifier.LIGHT_PACKAGE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_EXAMPLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_TEMPLATE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN_BUNDLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;
import static org.mule.tools.api.packager.packaging.Classifier.TEST_JAR;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

@RunWith(Parameterized.class)
public class AllowedDependencyValidatorTest {

  public static final String COMPILE = "compile";
  public static final String PROVIDED = "provided";
  private final boolean isAllowed;
  private final ArtifactCoordinates artifactCoordinates;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"mule-service", COMPILE, FALSE},
        {"mule-service", PROVIDED, TRUE},
        {"mule-server-plugin", COMPILE, FALSE},
        {"mule-server-plugin", PROVIDED, TRUE},

        {MULE_DOMAIN_BUNDLE.toString(), COMPILE, FALSE},
        {MULE_DOMAIN_BUNDLE.toString(), PROVIDED, FALSE},

        {MULE_DOMAIN.toString(), COMPILE, FALSE},
        {MULE_DOMAIN.toString(), PROVIDED, TRUE},

        {MULE_DOMAIN.toString() + "-" + LIGHT_PACKAGE.toString(), COMPILE, FALSE},
        {MULE_DOMAIN.toString() + "-" + LIGHT_PACKAGE.toString(), PROVIDED, TRUE},

        {MULE_DOMAIN.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE},
        {MULE_DOMAIN.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE},

        {MULE_DOMAIN.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE},
        {MULE_DOMAIN.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE},


        {MULE_POLICY.toString(), COMPILE, FALSE},
        {MULE_POLICY.toString(), PROVIDED, TRUE},

        {MULE_POLICY.toString() + "-" + LIGHT_PACKAGE.toString(), COMPILE, FALSE},
        {MULE_POLICY.toString() + "-" + LIGHT_PACKAGE.toString(), PROVIDED, TRUE},

        {MULE_POLICY.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE},
        {MULE_POLICY.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE},

        {MULE_POLICY.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE},
        {MULE_POLICY.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE},


        {MULE_APPLICATION.toString(), COMPILE, FALSE},
        {MULE_APPLICATION.toString(), PROVIDED, TRUE},

        {MULE_APPLICATION.toString() + "-" + LIGHT_PACKAGE.toString(), COMPILE, FALSE},
        {MULE_APPLICATION.toString() + "-" + LIGHT_PACKAGE.toString(), PROVIDED, TRUE},

        {MULE_APPLICATION.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE},
        {MULE_APPLICATION.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE},

        {MULE_APPLICATION.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE},
        {MULE_APPLICATION.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE},


        {MULE_APPLICATION_EXAMPLE.toString(), COMPILE, FALSE},
        {MULE_APPLICATION_EXAMPLE.toString(), PROVIDED, TRUE},

        {MULE_APPLICATION_EXAMPLE.toString() + "-" + LIGHT_PACKAGE.toString(), COMPILE, FALSE},
        {MULE_APPLICATION_EXAMPLE.toString() + "-" + LIGHT_PACKAGE.toString(), PROVIDED, TRUE},

        {MULE_APPLICATION_EXAMPLE.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE},
        {MULE_APPLICATION_EXAMPLE.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE},

        {MULE_APPLICATION_EXAMPLE.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE},
        {MULE_APPLICATION_EXAMPLE.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE},


        {MULE_APPLICATION_TEMPLATE.toString(), COMPILE, FALSE},
        {MULE_APPLICATION_TEMPLATE.toString(), PROVIDED, TRUE},

        {MULE_APPLICATION_TEMPLATE.toString() + "-" + LIGHT_PACKAGE.toString(), COMPILE, FALSE},
        {MULE_APPLICATION_TEMPLATE.toString() + "-" + LIGHT_PACKAGE.toString(), PROVIDED, TRUE},

        {MULE_APPLICATION_TEMPLATE.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE},
        {MULE_APPLICATION_TEMPLATE.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE},

        {MULE_APPLICATION_TEMPLATE.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), COMPILE, FALSE},
        {MULE_APPLICATION_TEMPLATE.toString() + "-" + LIGHT_PACKAGE.toString() + "-" + TEST_JAR.toString(), PROVIDED, TRUE},

    });
  }

  public AllowedDependencyValidatorTest(String classifier, String scope, Boolean isAllowed) {

    this.artifactCoordinates = buildFakeArtifactCoordinates();
    this.artifactCoordinates.setScope(scope);
    this.artifactCoordinates.setClassifier(classifier);
    this.isAllowed = isAllowed;
  }

  @Test
  public void areDependenciesAllowed() {
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

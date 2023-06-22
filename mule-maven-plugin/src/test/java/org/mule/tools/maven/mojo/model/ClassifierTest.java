/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.model;

import org.junit.jupiter.api.Test;
import org.mule.tools.api.packager.packaging.Classifier;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassifierTest {

  private static final String MULE_APPLICATION = "mule-application";
  private static final String MULE_APPLICATION_EXAMPLE = "mule-application-example";
  private static final String MULE_APPLICATION_TEMPLATE = "mule-application-template";
  private static final String MULE_DOMAIN = "mule-domain";
  private static final String MULE_POLICY = "mule-policy";

  @Test
  void fromStringTest() {
    assertThat(Classifier.fromString(MULE_APPLICATION)).as("Not the expected classifier").isEqualTo(Classifier.MULE_APPLICATION);
    assertThat(Classifier.fromString(MULE_APPLICATION_EXAMPLE)).as("Not the expected classifier")
        .isEqualTo(Classifier.MULE_APPLICATION_EXAMPLE);
    assertThat(Classifier.fromString(MULE_APPLICATION_TEMPLATE)).as("Not the expected classifier")
        .isEqualTo(Classifier.MULE_APPLICATION_TEMPLATE);
    assertThat(Classifier.fromString(MULE_DOMAIN)).as("Not the expected classifier").isEqualTo(Classifier.MULE_DOMAIN);
    assertThat(Classifier.fromString(MULE_POLICY)).as("Not the expected classifier").isEqualTo(Classifier.MULE_POLICY);
  }

  @Test
  void equalsTest() {
    assertThat(Classifier.MULE_APPLICATION.equals(MULE_APPLICATION)).as("Equals method did not behave as expected").isTrue();
    assertThat(Classifier.MULE_APPLICATION_EXAMPLE.equals(MULE_APPLICATION_EXAMPLE))
        .as("Equals method did not behave as expected").isTrue();
    assertThat(Classifier.MULE_APPLICATION_TEMPLATE.equals(MULE_APPLICATION_TEMPLATE))
        .as("Equals method did not behave as expected").isTrue();
    assertThat(Classifier.MULE_DOMAIN.equals(MULE_DOMAIN)).as("Equals method did not behave as expected").isTrue();
    assertThat(Classifier.MULE_POLICY.equals(MULE_POLICY)).as("Equals method did not behave as expected").isTrue();
  }
}

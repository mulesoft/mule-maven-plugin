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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.mule.tools.api.packager.packaging.Classifier;

public class ClassifierTest {

  private static final String MULE_APPLICATION = "mule-application";
  private static final String MULE_APPLICATION_EXAMPLE = "mule-application-example";
  private static final String MULE_APPLICATION_TEMPLATE = "mule-application-template";
  private static final String MULE_DOMAIN = "mule-domain";
  private static final String MULE_POLICY = "mule-policy";

  @Test
  public void fromStringTest() {
    assertThat("Not the expected classifier", Classifier.fromString(MULE_APPLICATION), equalTo(Classifier.MULE_APPLICATION));
    assertThat("Not the expected classifier", Classifier.fromString(MULE_APPLICATION_EXAMPLE),
               equalTo(Classifier.MULE_APPLICATION_EXAMPLE));
    assertThat("Not the expected classifier", Classifier.fromString(MULE_APPLICATION_TEMPLATE),
               equalTo(Classifier.MULE_APPLICATION_TEMPLATE));
    assertThat("Not the expected classifier", Classifier.fromString(MULE_DOMAIN), equalTo(Classifier.MULE_DOMAIN));
    assertThat("Not the expected classifier", Classifier.fromString(MULE_POLICY), equalTo(Classifier.MULE_POLICY));
  }

  @Test
  public void equalsTest() {
    assertThat("Equals method did not behave as expected", Classifier.MULE_APPLICATION.equals(MULE_APPLICATION));
    assertThat("Equals method did not behave as expected",
               Classifier.MULE_APPLICATION_EXAMPLE.equals(MULE_APPLICATION_EXAMPLE));
    assertThat("Equals method did not behave as expected",
               Classifier.MULE_APPLICATION_TEMPLATE.equals(MULE_APPLICATION_TEMPLATE));
    assertThat("Equals method did not behave as expected", Classifier.MULE_DOMAIN.equals(MULE_DOMAIN));
    assertThat("Equals method did not behave as expected", Classifier.MULE_POLICY.equals(MULE_POLICY));
  }
}

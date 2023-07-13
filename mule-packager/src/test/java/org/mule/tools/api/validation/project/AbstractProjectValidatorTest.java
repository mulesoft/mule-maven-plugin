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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mule.tools.api.validation.project.AbstractProjectValidator.isPackagingTypeValid;
import org.mule.tools.api.exception.ValidationException;
import org.junit.jupiter.api.Test;

public class AbstractProjectValidatorTest {

  protected static final String GROUP_ID = "group-id";
  protected static final String ARTIFACT_ID = "artifact-id";
  protected static final String MULE_APPLICATION = "mule-application";
  protected static final String MULE_DOMAIN = "mule-domain";
  protected static final String MULE_POLICY = "mule-policy";

  @Test
  public void isMuleApplicationPackagingTypeValidTest() throws ValidationException {
    assertThat(isPackagingTypeValid(MULE_APPLICATION)).describedAs("Packaging type should be valid").isTrue();
  }

  @Test
  public void isMuleDomainPackagingTypeValidTest() throws ValidationException {
    assertThat(isPackagingTypeValid(MULE_DOMAIN)).describedAs("Packaging type should be valid").isTrue();
  }

  @Test
  public void isMulePolicyPackagingTypeValidTest() throws ValidationException {
    assertThat(isPackagingTypeValid(MULE_POLICY)).describedAs("Packaging type should be valid").isTrue();
  }

  @Test
  public void isPackagingTypeValidInvalidPackagingTest() {
    assertThrows(ValidationException.class, () -> isPackagingTypeValid("no-valid-packaging"));
  }

  @Test
  public void isPackagingTypeValidNullTest() {
    Exception exception = assertThrows(ValidationException.class, () -> isPackagingTypeValid(null));
    String expectedMessage = "Packaging type name should not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

}

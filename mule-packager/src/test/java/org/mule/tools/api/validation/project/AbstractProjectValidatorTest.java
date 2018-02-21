/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.project;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.tools.api.validation.project.AbstractProjectValidator.isPackagingTypeValid;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mule.tools.api.exception.ValidationException;

public class AbstractProjectValidatorTest {

  protected static final String MULE_DOMAIN = "mule-domain";
  private static final String MULE = "mule";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void isMuleApplicationPackagingTypeValidTest() throws ValidationException {
    assertThat("Packaging type should be valid", isPackagingTypeValid(MULE), is(true));
  }

  @Test
  public void isMuleDomainPackagingTypeValidTest() throws ValidationException {
    assertThat("Packaging type should be valid", isPackagingTypeValid(MULE_DOMAIN), is(true));
  }

  @Test
  public void isPackagingTypeValidInvalidPackagingTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    isPackagingTypeValid("no-valid-packaging");
  }

  @Test
  public void isPackagingTypeValidNullTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("Packaging type name should not be null");
    isPackagingTypeValid(null);
  }

}

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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.tools.api.validation.AbstractProjectValidator.isPackagingTypeValid;
import static org.mule.tools.api.validation.AbstractProjectValidator.isProjectVersionValid;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mule.tools.api.exception.ValidationException;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AbstractProjectValidatorTest {

  protected static final String GROUP_ID = "group-id";
  protected static final String ARTIFACT_ID = "artifact-id";
  protected static final String MULE_APPLICATION = "mule-application";
  protected static final String MULE_DOMAIN = "mule-domain";
  protected static final String MULE_POLICY = "mule-policy";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void isMuleApplicationPackagingTypeValidTest() throws ValidationException {
    assertThat("Packaging type should be valid", isPackagingTypeValid(MULE_APPLICATION), is(true));
  }

  @Test
  public void isMuleDomainPackagingTypeValidTest() throws ValidationException {
    assertThat("Packaging type should be valid", isPackagingTypeValid(MULE_DOMAIN), is(true));
  }

  @Test
  public void isMulePolicyPackagingTypeValidTest() throws ValidationException {
    assertThat("Packaging type should be valid", isPackagingTypeValid(MULE_POLICY), is(true));
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

  @Test
  public void isProjectVersionValidTest() throws ValidationException {
    for (String validVersion : getValidVersions()) {
      try {
        isProjectVersionValid(validVersion);
      } catch (ValidationException e) {
        fail(validVersion + " should be a valid version");
      }
    }
  }

  @Test
  public void isProjectVersionValidFailTest() {
    for (String invalidVersion : getInvalidVersions()) {
      try {
        isProjectVersionValid(invalidVersion);
        fail(invalidVersion + " should be a invalid version");
      } catch (ValidationException e) {
      }
    }
  }

  List<String> getValidVersions() {
    List<String> validVersions = new ArrayList<>();
    validVersions.add("0.0.0");
    validVersions.add("0.1.0");
    validVersions.add("0.1.1");
    validVersions.add("1.4.0");
    validVersions.add("0.44.0");
    validVersions.add("111.1.0");
    validVersions.add("432.0.43");
    validVersions.add("0.0.114765");
    validVersions.add("0.1.0-SNAPSHOT");
    validVersions.add("0.1.0-rc-SNAPSHOT");
    validVersions.add("0.1.0-rc.SNAPSHOT");
    validVersions.add("0.1.0+sha.12343");
    return validVersions;
  }


  List<String> getInvalidVersions() {
    List<String> invalidVersions = new ArrayList<>();
    invalidVersions.add("0");
    invalidVersions.add("0.1");
    invalidVersions.add("1.0");
    invalidVersions.add("0..");
    invalidVersions.add("..0");
    invalidVersions.add(".4.");
    invalidVersions.add("1.3.01");
    invalidVersions.add("0.00.1");
    invalidVersions.add("3.4.04");
    invalidVersions.add("3.4.44.3");
    invalidVersions.add("a3.4.4");
    invalidVersions.add("a.4.7");
    invalidVersions.add("a.b.c");
    invalidVersions.add("1.0.0-");
    invalidVersions.add("1.0.0-#");
    invalidVersions.add("1.0.0-sdfsd#dsfds");
    invalidVersions.add("1.0.0-sdfsd^dsfds");
    invalidVersions.add("1.0.0-sdfsd&dsfds");
    invalidVersions.add("1.0.0-sdfsd@dsfds");
    invalidVersions.add("1.0.0-sdfsd!dsfds");
    return invalidVersions;
  }
}

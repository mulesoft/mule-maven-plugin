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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mule.tools.api.validation.AbstractProjectValidator.isProjectVersionValid;
import static org.mule.tools.api.validation.VersionUtils.completeIncremental;
import static org.mule.tools.api.validation.VersionUtils.isVersionGraterOrEquals;
import static org.mule.tools.api.validation.VersionUtils.isVersionValid;

import org.junit.Test;

import org.mule.tools.api.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class VersionUtilsTest {

  @Test(expected = ValidationException.class)
  public void isVersionGraterOrEqualsIllegalVersion1() throws ValidationException {
    String version1 = "3.3.9.9";
    String version2 = "3.3.2";

    isVersionGraterOrEquals(version1, version2);
  }

  @Test(expected = ValidationException.class)
  public void isVersionGraterOrEqualsIllegalVersion2() throws ValidationException {
    String version1 = "3.3.9";
    String version2 = "3.3.2.50";

    isVersionGraterOrEquals(version1, version2);
  }

  @Test
  public void isVersionGraterOrEqualsTrue() throws ValidationException {
    String version1 = "3.3.9";
    String version2 = "3.3.2";

    assertThat(isVersionGraterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsTrueVersion2Incomplete() throws ValidationException {
    String version1 = "3.3.9";
    String version2 = "3.3";

    assertThat(isVersionGraterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsFalse() throws ValidationException {
    String version1 = "3.3.2";
    String version2 = "3.3.9";

    assertThat(isVersionGraterOrEquals(version1, version2), is(false));
  }

  @Test
  public void isVersionGraterOrEqualsFalseVersion1Incomplete() throws ValidationException {
    String version1 = "3.3";
    String version2 = "3.3.9";

    assertThat(isVersionGraterOrEquals(version1, version2), is(false));
  }

  @Test
  public void isVersionGraterOrEqualsSnapshotTrue() throws ValidationException {
    String version1 = "3.3.9-SNAPSHOT";
    String version2 = "3.3.2";

    assertThat(isVersionGraterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsSNAPSHOTFalse() throws ValidationException {
    String version1 = "3.3.2";
    String version2 = "3.3.9-SNAPSHOT";

    assertThat(isVersionGraterOrEquals(version1, version2), is(false));
  }

  @Test
  public void completeIncrementalString() throws ValidationException {
    String version = "3.3";
    assertThat(completeIncremental(version), is(version + ".0"));
  }

  @Test
  public void completeIncrementalStringWithQualifier() throws ValidationException {
    String version = "3.3";
    String qualifier = "-SNAPSHOT";
    assertThat(completeIncremental(version + qualifier), is(version + ".0"));
  }

  @Test
  public void completeIncrementalStringNoChanges() throws ValidationException {
    String version = "3.3.2";
    assertThat(completeIncremental(version), is(version));
  }

  @Test
  public void completeIncrementalStringNoChangesWithQualifier() throws ValidationException {
    String version = "3.3.2";
    String qualifier = "-SNAPSHOT";

    assertThat(completeIncremental(version + qualifier), is(version));
  }

  @Test
  public void isProjectVersionValidTest() throws ValidationException {
    for (String validVersion : getValidVersions()) {
      if (!isVersionValid(validVersion)) {
        fail(validVersion + " should be a valid version");
      }
    }
  }

  @Test
  public void isProjectVersionValidFailTest() {
    for (String invalidVersion : getInvalidVersions()) {
      if (isVersionValid(invalidVersion)) {
        fail(invalidVersion + " should be a invalid version");
      }
    }
  }

  private List<String> getValidVersions() {
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


  private List<String> getInvalidVersions() {
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

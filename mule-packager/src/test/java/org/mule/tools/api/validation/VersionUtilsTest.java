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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mule.tools.api.validation.VersionUtils.*;

import org.junit.Test;

import org.mule.tools.api.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class VersionUtilsTest {

  @Test(expected = ValidationException.class)
  public void isVersionGraterOrEqualsIllegalVersion1() throws ValidationException {
    String version1 = "3.3.9.9";
    String version2 = "3.3.2";

    isVersionGreaterOrEquals(version1, version2);
  }

  @Test(expected = ValidationException.class)
  public void isVersionGraterOrEqualsIllegalVersion2() throws ValidationException {
    String version1 = "3.3.9";
    String version2 = "3.3.2.50";

    isVersionGreaterOrEquals(version1, version2);
  }

  @Test
  public void isVersionGraterOrEqualsTrue() throws ValidationException {
    String version1 = "3.3.9";
    String version2 = "3.3.2";

    assertThat(isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsTrue1() throws ValidationException {
    String version1 = "3.5.0";
    String version2 = "3.3.3";

    assertThat(isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsTrue2() throws ValidationException {
    String version1 = "3.0.0";
    String version2 = "2.1.3";

    assertThat(isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsFalse1() throws ValidationException {
    String version1 = "3.0.5";
    String version2 = "3.3.3";

    assertThat(isVersionGreaterOrEquals(version1, version2), is(false));
  }

  @Test
  public void isVersionGraterOrEqualsFalse2() throws ValidationException {
    String version1 = "3.0.0";
    String version2 = "4.0.0";

    assertThat(isVersionGreaterOrEquals(version1, version2), is(false));
  }

  @Test
  public void isVersionGraterOrEqualsTrueVersion2Incomplete() throws ValidationException {
    String version1 = "3.3.9";
    String version2 = "3.3";

    assertThat(isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsFalse() throws ValidationException {
    String version1 = "3.3.2";
    String version2 = "3.3.9";

    assertThat(isVersionGreaterOrEquals(version1, version2), is(false));
  }

  @Test
  public void isVersionGraterOrEqualsFalseVersion1Incomplete() throws ValidationException {
    String version1 = "3.3";
    String version2 = "3.3.9";

    assertThat(isVersionGreaterOrEquals(version1, version2), is(false));
  }

  @Test
  public void isVersionGraterOrEqualsSnapshotTrue() throws ValidationException {
    String version1 = "3.3.9-SNAPSHOT";
    String version2 = "3.3.2";

    assertThat(isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsSNAPSHOTFalse() throws ValidationException {
    String version1 = "3.3.2";
    String version2 = "3.3.9-SNAPSHOT";

    assertThat(isVersionGreaterOrEquals(version1, version2), is(false));
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
    invalidVersions.add("0.0.0");
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

  @Test
  public void getBaseVersionTest() {
    Map<String, String> testInput = getBaseVersionTestInput();
    for (String version : testInput.keySet()) {
      String expectedBaseVersion = testInput.get(version);
      assertThat("Base version is not the expected", getBaseVersion(version), equalTo(expectedBaseVersion));
    }
  }

  private Map<String, String> getBaseVersionTestInput() {
    Map<String, String> versions = new HashMap<>();
    versions.put("0.1.0", "0.1.0");
    versions.put("0.1.1", "0.1.1");
    versions.put("1.4.0", "1.4.0");
    versions.put("0.44.0", "0.44.0");
    versions.put("111.1.0", "111.1.0");
    versions.put("432.0.43", "432.0.43");
    versions.put("0.0.114765", "0.0.114765");
    versions.put("0.1.0-SNAPSHOT", "0.1.0");
    versions.put("0.1.0-rc-SNAPSHOT", "0.1.0");
    versions.put("0.1.0-rc.SNAPSHOT", "0.1.0");
    versions.put("0.1.0+sha.12343", "0.1.0");
    return versions;
  }

  @Test
  public void getMajorTest() {
    Map<String, String> testInput = getMajorTestInput();
    for (String version : testInput.keySet()) {
      String expectedMajor = testInput.get(version);
      assertThat("Major is not the expected", getMajor(version), equalTo(expectedMajor));
    }
  }

  private Map<String, String> getMajorTestInput() {
    Map<String, String> versions = new HashMap<>();
    versions.put("0.1.0", "0");
    versions.put("0.1.1", "0");
    versions.put("1.4.0", "1");
    versions.put("0.44.0", "0");
    versions.put("111.1.0", "111");
    versions.put("432.0.43", "432");
    versions.put("0.0.114765", "0");
    versions.put("0.1.0-SNAPSHOT", "0");
    versions.put("0.1.0-rc-SNAPSHOT", "0");
    versions.put("0.1.0-rc.SNAPSHOT", "0");
    versions.put("0.1.0+sha.12343", "0");
    return versions;
  }

  @Test
  public void completeIncrementalTest() throws ValidationException {
    Map<String, String> testInput = completeIncrementalTestInput();
    for (String version : testInput.keySet()) {
      String expectedVersion = testInput.get(version);
      assertThat("Completed version is not the expected", completeIncremental(version), equalTo(expectedVersion));
    }
  }

  private Map<String, String> completeIncrementalTestInput() {
    Map<String, String> versions = new HashMap<>();
    versions.put("1", "1.0.0");
    versions.put("0.1", "0.1.0");
    versions.put("0.1.1", "0.1.1");
    versions.put("0.1-SNAPSHOT", "0.1.0");
    versions.put("0.1-rc-SNAPSHOT", "0.1.0");
    versions.put("0.1-rc.SNAPSHOT", "0.1.0");
    versions.put("1.1+sha.12343", "1.1.0");
    return versions;
  }

  @Test
  public void isRangeTest() {
    Map<String, Boolean> testInput = isRangeTestInput();
    for (String version : testInput.keySet()) {
      Boolean expectedResult = testInput.get(version);
      assertThat("Expected result is not the expected", isRange(version), equalTo(expectedResult));
    }
  }

  private Map<String, Boolean> isRangeTestInput() {
    Map<String, Boolean> ranges = new HashMap<>();
    ranges.put("(,1.0]", true);
    ranges.put("[1.0]", true);
    ranges.put("[1.2,1.3]", true);
    ranges.put("[1.0,2.0)", true);
    ranges.put("(,1.0],[1.2,)", true);
    ranges.put("(,1.1),(1.1,)", true);
    ranges.put("1.0", false);
    ranges.put("0.1-SNAPSHOT", false);
    ranges.put("0.1-rc.SNAPSHOT", false);
    ranges.put("1.1+sha.12343", false);
    return ranges;
  }

}

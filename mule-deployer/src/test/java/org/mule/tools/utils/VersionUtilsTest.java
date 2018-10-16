/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.utils;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.tools.utils.VersionUtils.isGreaterThanOrSameVersion;
import static org.mule.tools.utils.VersionUtils.isSameVersion;

@RunWith(Enclosed.class)
public class VersionUtilsTest {

  private static final String VERSION_A = "4.0.0";
  private static final String VERSION_B = "4.0.1";


  @RunWith(Parameterized.class)
  public static class IsSameVersionTest {

    private final String version1;
    private final String version2;
    private final Boolean expectedResult;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {
          {VERSION_A, VERSION_A, Boolean.TRUE},
          {VERSION_A, VERSION_B, Boolean.FALSE},
          {VERSION_B, VERSION_A, Boolean.FALSE},
          {null, VERSION_A, Boolean.FALSE},
          {VERSION_A, null, Boolean.FALSE},
          {null, null, Boolean.TRUE}
      });
    }

    public IsSameVersionTest(String version1, String version2, Boolean expectedResult) {
      this.version1 = version1;
      this.version2 = version2;
      this.expectedResult = expectedResult;
    }

    @Test
    public void isSameVersionTest() {
      assertThat("isSameVersion didn't return the expected return value when called with parameters " + version1 + " and "
          + version2, isSameVersion(version1, version2), equalTo(expectedResult));
    }
  }

  @RunWith(Parameterized.class)
  public static class IsGreaterThanOrSameTest {

    private final String version1;
    private final String version2;
    private final Boolean expectedResult;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {
          {VERSION_A, VERSION_A, Boolean.TRUE},
          {VERSION_A, VERSION_B, Boolean.FALSE},
          {VERSION_B, VERSION_A, Boolean.TRUE},
          {null, VERSION_A, Boolean.FALSE},
          {VERSION_A, null, Boolean.FALSE},
          {null, null, Boolean.TRUE}
      });
    }

    public IsGreaterThanOrSameTest(String version1, String version2, Boolean expectedResult) {
      this.version1 = version1;
      this.version2 = version2;
      this.expectedResult = expectedResult;
    }

    @Test
    public void isSameVersionTest() {
      assertThat("isGreaterThanOrSameVersion didn't return the expected return value when called with parameters " + version1
          + " and "
          + version2, isGreaterThanOrSameVersion(version1, version2), equalTo(expectedResult));
    }
  }

}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mule.tools.utils.VersionUtils.isSameVersion;

public class VersionUtilsTest {

  private static final String VERSION_A = "4.0.0";
  private static final String VERSION_B = "4.0.1";

  public static Stream<Arguments> data() {
    return Stream.of(Arguments.of(VERSION_A, VERSION_A, Boolean.TRUE),
                     Arguments.of(VERSION_A, VERSION_B, Boolean.FALSE),
                     Arguments.of(VERSION_B, VERSION_A, Boolean.FALSE),
                     Arguments.of(null, VERSION_A, Boolean.FALSE),
                     Arguments.of(VERSION_A, null, Boolean.FALSE),
                     Arguments.of(null, null, Boolean.TRUE));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void isSameVersionTest(String version1, String version2, Boolean expectedResult) {
    assertThat(isSameVersion(version1, version2))
        .describedAs("isSameVersion didn't return the expected return value when called with parameters " + version1 + " and "
            + version2)
        .isEqualTo(expectedResult);
  }

}

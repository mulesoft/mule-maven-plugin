/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentSupportedVersionsTest {

  public static Stream<Arguments> data() {
    return Stream.of(Arguments.of("4.0.0", new EnvironmentSupportedVersions(newArrayList("4.0.0")), Boolean.TRUE),
                     Arguments.of("4.0.0", new EnvironmentSupportedVersions(newArrayList("4.0.1")), Boolean.FALSE),
                     Arguments.of("4.0.0", new EnvironmentSupportedVersions(newArrayList("4.0.0", "4.0.1")), Boolean.TRUE),
                     Arguments.of("4.0.1", new EnvironmentSupportedVersions(newArrayList("4.0.0", "4.0.1")), Boolean.TRUE),
                     Arguments.of("3.0.0", new EnvironmentSupportedVersions(newArrayList("4.0.0", "4.0.1")), Boolean.FALSE),
                     Arguments.of("3.0.0-SNAPSHOT",
                                  new EnvironmentSupportedVersions(newArrayList("3.0.0-SNAPSHOT", "4.0.0", "4.0.1")),
                                  Boolean.TRUE),
                     Arguments.of("3.0.0-SNAPSHOT", new EnvironmentSupportedVersions(newArrayList("3.0.0")), Boolean.FALSE));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void supportsTest(String version, EnvironmentSupportedVersions supportedVersion, Boolean isSupported) {
    assertThat(supportedVersion.supports(version))
        .describedAs("The version " + version + " should " + (isSupported ? "" : " not ") + " be supported.")
        .isEqualTo(isSupported);
  }
}

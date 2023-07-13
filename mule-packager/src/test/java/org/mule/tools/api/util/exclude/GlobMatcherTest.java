/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util.exclude;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobMatcherTest {

  public static Stream<Arguments> data() {
    return Stream.of(
                     // Positive tests
                     Arguments.of("*.java", "/User/lala/src/main/mule/blabla.java", Boolean.TRUE),
                     Arguments.of("*.*", "/User/lala/src/main/mule/.project", Boolean.TRUE),
                     Arguments.of("*.{java,class}", "/User/lala/src/main/mule/blabla.java", Boolean.TRUE),
                     Arguments.of("*.{java,class}", "/User/lala/src/main/mule/blabla.class", Boolean.TRUE),
                     Arguments.of("foo-test.???", "/User/lala/src/test/munit/foo-test.xml", Boolean.TRUE),
                     Arguments.of("/user/*/*", "/user/lala/bla.txt", Boolean.TRUE),
                     Arguments.of("/user/*/*", "/user/lala/src/", Boolean.TRUE),
                     Arguments.of("/user/**", "/user/lala/src", Boolean.TRUE),
                     Arguments.of("C:\\\\*", "C:\\foo", Boolean.TRUE),
                     // Negative tests
                     Arguments.of("*.cpp", "/User/lala/src/main/mule/blabla.java", Boolean.FALSE),
                     Arguments.of("*.*", "/User/lala/src/main/mu.le/project", Boolean.FALSE),
                     Arguments.of("*.{java,class}", "/User/class/src/main/mule/blabla.cpp", Boolean.FALSE),
                     Arguments.of("*.{java,class}", "/User/java/src/main/mule/blabla.cpp", Boolean.FALSE),
                     Arguments.of("foo-test.???", "/User/lala/src/test/munit/foo-test.html", Boolean.FALSE),
                     Arguments.of("/user/*/*", "/user/lala/src/blabla", Boolean.FALSE),
                     Arguments.of("/user/**", "/user/", Boolean.FALSE),
                     Arguments.of("/user/**", "/user", Boolean.FALSE),
                     Arguments.of("C:\\\\**", "C:\\foo\\lala", Boolean.TRUE));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void matchTest(GlobMatcher matcher, Path path, boolean expectedResult) {
    assertThat(matcher.matches(path))
        .describedAs("The matcher should have returned " + !expectedResult + " to path " + path.toString())
        .isEqualTo(expectedResult);
  }
}

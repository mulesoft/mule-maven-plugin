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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.tools.client.standalone.controller.probing.deployment.ApplicationDeploymentProbe;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Parameterized.class)
public class GlobMatcherTest {

  private final GlobMatcher matcher;
  private final Path path;
  private final boolean expectedResult;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        // Positive tests
        {"*.java", "/User/lala/src/main/mule/blabla.java", Boolean.TRUE},
        {"*.*", "/User/lala/src/main/mule/.project", Boolean.TRUE},
        {"*.{java,class}", "/User/lala/src/main/mule/blabla.java", Boolean.TRUE},
        {"*.{java,class}", "/User/lala/src/main/mule/blabla.class", Boolean.TRUE},
        {"foo-test.???", "/User/lala/src/test/munit/foo-test.xml", Boolean.TRUE},
        {"/user/*/*", "/user/lala/bla.txt", Boolean.TRUE},
        {"/user/*/*", "/user/lala/src/", Boolean.TRUE},
        {"/user/**", "/user/lala/src", Boolean.TRUE},
        {"C:\\\\*", "C:\\foo", Boolean.TRUE},
        // Negative tests
        {"*.cpp", "/User/lala/src/main/mule/blabla.java", Boolean.FALSE},
        {"*.*", "/User/lala/src/main/mu.le/project", Boolean.FALSE},
        {"*.{java,class}", "/User/class/src/main/mule/blabla.cpp", Boolean.FALSE},
        {"*.{java,class}", "/User/java/src/main/mule/blabla.cpp", Boolean.FALSE},
        {"foo-test.???", "/User/lala/src/test/munit/foo-test.html", Boolean.FALSE},
        {"/user/*/*", "/user/lala/src/blabla", Boolean.FALSE},
        {"/user/**", "/user/", Boolean.FALSE},
        {"/user/**", "/user", Boolean.FALSE},
        {"C:\\\\**", "C:\\foo\\lala", Boolean.TRUE},
    });
  }

  public GlobMatcherTest(String pattern, String path, Boolean expectedResult) {
    this.matcher = new GlobMatcher(pattern);
    this.path = Paths.get(path);
    this.expectedResult = expectedResult;
  }

  @Test
  public void matchTest() {
    assertThat("The matcher should have returned " + !expectedResult + " to path " + path.toString(), matcher.matches(path),
               is(expectedResult));
  }
}

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

import java.nio.file.Path;
import java.nio.file.PathMatcher;

import static java.nio.file.FileSystems.getDefault;

class GlobMatcher implements PathMatcher {

  private static final String SYNTAX = "glob";

  public static final GlobMatcher CLASSPATH_FILE_GLOB_MATCHER = new GlobMatcher(".classpath");
  public static final GlobMatcher PROJECT_FILE_GLOB_MATCHER = new GlobMatcher(".project");

  private final PathMatcher matcher;

  public GlobMatcher(String pattern) {
    matcher = getDefault().getPathMatcher(SYNTAX + ":" + pattern);
  }

  public boolean matches(Path file) {
    return file == null || matcher.matches(file) || matcher.matches(file.getFileName());
  }
}

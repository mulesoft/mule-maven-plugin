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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Files.readLines;

/**
 * Matcher for mule exclusions.
 * 
 * Parses the _muleExclusion file present in the project base folder to create a list of path matchers. The file must contain
 * pathnames specified in a glob pattern. By default, .classpath and .project files always match against this matcher.
 */
public class MuleExclusionMatcher implements PathMatcher {

  protected static final String MULE_EXCLUDE_FILENAME = "_muleExclude";
  public static final GlobMatcher CLASSPATH_FILE_MATCHER = new GlobMatcher(".classpath");
  public static final GlobMatcher PROJECT_FILE_MATCHER = new GlobMatcher(".project");

  private List<GlobMatcher> muleExcludeMatchers =
      newArrayList(CLASSPATH_FILE_MATCHER, PROJECT_FILE_MATCHER);

  public MuleExclusionMatcher() {}

  /**
   * Creates a muleExclusionMatcher based on the _muleExcludes file.
   *
   * @param projectBaseFolder Project base folder path
   */
  public MuleExclusionMatcher(Path projectBaseFolder) throws IOException {
    checkArgument(projectBaseFolder != null, "Project base folder should not be null");
    parse(new File(projectBaseFolder.toFile(), MULE_EXCLUDE_FILENAME));
  }

  private void parse(File file) throws IOException {
    if (file.exists()) {
      muleExcludeMatchers.addAll(readLines(file, Charset.defaultCharset(), new GlobMatcherFileReader()));
    }
  }

  /**
   * Checks if the path should be excluded according to patterns in the mule exclude file.
   * 
   * @param file The path to be checked
   * @return true either if the path is null or if it matches at least one of the path exclusions
   */
  public boolean matches(Path file) {
    return muleExcludeMatchers.stream().anyMatch(pattern -> pattern.matches(file));
  }
}

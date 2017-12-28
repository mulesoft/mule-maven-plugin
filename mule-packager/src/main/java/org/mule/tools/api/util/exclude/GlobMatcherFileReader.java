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

import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.strip;

/**
 * Reads a _muleExclude file and generates a list of {@link GlobMatcher}.
 * 
 * <pre>
 *     
 * The file can contain three types of lines:
 * (1) Empty - zero or more empty spaces
 * (2) Comment - first non-space character must be the comment delimiter #
 * (3) Glob pattern - using the glob syntax.
 *
 * (1) and (2) are ignored by the reader. (3) are used to generate a matcher.
 *
 * </pre>
 * 
 * This class is meant to be used as a callback by the readLines method of the {@link com.google.common.io.Files} class.
 */
class GlobMatcherFileReader implements LineProcessor<List<GlobMatcher>> {

  private List<GlobMatcher> globMatchers = new ArrayList<>();

  @Override
  public boolean processLine(String line) throws IOException {
    line = strip(line);
    if (isNotBlank(line) && !startsWith(line, "#")) {
      return globMatchers.add(new GlobMatcher(line));
    }
    return true;
  }

  @Override
  public List<GlobMatcher> getResult() {
    return globMatchers;
  }
}

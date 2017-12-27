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

import com.beust.jcommander.internal.Lists;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.strip;

class GlobMatcherFileReader implements LineProcessor<List<GlobMatcher>> {

  private List<GlobMatcher> globMatchers = Lists.newArrayList();

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

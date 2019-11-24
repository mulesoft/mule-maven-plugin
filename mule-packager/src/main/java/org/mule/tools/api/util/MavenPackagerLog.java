/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.util;

import org.apache.maven.plugin.logging.Log;
import org.mule.tools.api.util.PackagerLog;

public class MavenPackagerLog implements PackagerLog {

  private Log log;

  public MavenPackagerLog(Log log) {
    this.log = log;
  }

  @Override
  public void info(String s) {
    log.info(s);
  }

  @Override
  public void error(String s) {
    log.error(s);
  }

  @Override
  public void warn(String s) {
    log.warn(s);
  }

  @Override
  public void debug(String s) {
    log.debug(s);
  }

  @Override
  public void error(String s, Throwable e) {
    log.error(s, e);
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

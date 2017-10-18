/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.deploy.logging;

import org.apache.maven.plugin.logging.Log;
import org.mule.tools.utils.DeployerLog;

public class MavenDeployerLog implements DeployerLog {

  private Log log;

  public MavenDeployerLog(Log log) {
    this.log = log;
  }

  @Override
  public void info(String charSequence) {
    log.info(charSequence);
  }

  @Override
  public void error(String charSequence) {
    log.error(charSequence);
  }

  @Override
  public void warn(String charSequence) {
    log.warn(charSequence);
  }

  @Override
  public void debug(String charSequence) {
    log.debug(charSequence);
  }

  @Override
  public void error(String charSequence, Throwable e) {
    log.error(charSequence, e);
  }

  @Override
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }
}

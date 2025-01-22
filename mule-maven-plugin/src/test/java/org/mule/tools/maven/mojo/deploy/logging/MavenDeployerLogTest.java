/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.deploy.logging;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MavenDeployerLogTest {

  @Test
  void checkCall() {
    Log mock = mock(Log.class);
    MavenDeployerLog log = new MavenDeployerLog(mock);
    log.isDebugEnabled();
    log.info("test");
    log.error("test");
    log.error("test", mock(Throwable.class));
    log.warn("test");
    log.debug("test");


    verify(mock).isDebugEnabled();
    verify(mock).info(anyString());
    verify(mock).error(anyString());
    verify(mock).error(anyString(), any(Throwable.class));
    verify(mock).warn(anyString());
    verify(mock).debug(anyString());
  }
}

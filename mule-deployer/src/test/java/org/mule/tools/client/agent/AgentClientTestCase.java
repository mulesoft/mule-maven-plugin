/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.agent;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class AgentClientTestCase {

  private static final File APP = new File("/tmp/echo-test4.zip");
  private AgentClient agentClient;

  @Before
  public void setup() {
    agentClient = new AgentClient(null, "http://localhost:9999/");
  }

  @Test
  public void deployApplication() {
    agentClient.deployApplication("test", APP);
  }

  @Test
  public void undeployApplication() {
    agentClient.undeployApplication("echo-test4");
  }

}

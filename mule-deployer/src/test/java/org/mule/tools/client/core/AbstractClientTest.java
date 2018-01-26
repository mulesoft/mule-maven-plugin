/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * @author Mulesoft Inc.
 * @since 3.0.0
 */
public class AbstractClientTest {

  public static class TesteableClient extends AbstractClient {

    protected void init() {}

  }

  @Test
  public void getUserAgentMuleDeployer() {
    AbstractClient client = new TesteableClient();
    String userAgent = client.getUserAgent();
    assertThat(userAgent, is("mule-deployer"));
  }

}

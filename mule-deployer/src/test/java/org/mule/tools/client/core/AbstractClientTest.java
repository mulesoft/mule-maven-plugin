/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mulesoft Inc.
 * @since 3.0.0
 */
class AbstractClientTest {

  public static class TesteableClient extends AbstractClient {

    protected void init() {}

  }

  @Test
  void getUserAgentMuleDeployer() {
    AbstractClient client = new TesteableClient();
    String userAgent = client.getUserAgent();
    assertThat(userAgent).isEqualTo("mule-deployer");
  }

}

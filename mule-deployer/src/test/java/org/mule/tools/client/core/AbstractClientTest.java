/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

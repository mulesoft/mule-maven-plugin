/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.core;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mule.tools.client.core.AbstractClient.*;


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

  @Test
  void getClientWithJDKConnectorProvider() {
    System.setProperty(CONNECTOR_PROVIDER_PROPERTY, JDK);
    AbstractClient client = new TesteableClient();
    assertThat(((ClientConfig) client.getTarget("", "").getConfiguration()).getConnectorProvider().getClass())
        .isEqualTo(JdkConnectorProvider.class);
  }

  @Test
  void getClientWithHttpConnectorProvider() {
    System.setProperty(CONNECTOR_PROVIDER_PROPERTY, HTTP_URL);
    AbstractClient client = new TesteableClient();
    assertThat(((ClientConfig) client.getTarget("", "").getConfiguration()).getConnectorProvider().getClass())
        .isEqualTo(HttpUrlConnectorProvider.class);
  }

  @Test
  void getClientWithApacheConnectorProvider() {
    System.setProperty(CONNECTOR_PROVIDER_PROPERTY, APACHE_5);
    AbstractClient client = new TesteableClient();
    assertThat(((ClientConfig) client.getTarget("", "").getConfiguration()).getConnectorProvider().getClass())
        .isEqualTo(Apache5ConnectorProvider.class);
  }

  @Test
  void getClientWithDefaultConnectorProvider() {
    System.clearProperty(CONNECTOR_PROVIDER_PROPERTY);
    AbstractClient client = new TesteableClient();
    assertThat(((ClientConfig) client.getTarget("", "").getConfiguration()).getConnectorProvider().getClass())
        .isEqualTo(JdkConnectorProvider.class);
  }
}

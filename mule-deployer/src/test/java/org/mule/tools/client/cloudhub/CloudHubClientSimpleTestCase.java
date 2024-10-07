/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.cloudhub;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.SimpleHttpServer;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class CloudHubClientSimpleTestCase {

  private static final CloudHubDeployment CLOUDHUB_DEPLOYMENT = new CloudHubDeployment();
  private static final String USERNAME = System.getProperty("username", UUID.randomUUID().toString());
  private static final String PASSWORD = System.getProperty("password", UUID.randomUUID().toString());
  private static final String ENVIRONMENT = System.getProperty("environment", UUID.randomUUID().toString());
  private static final String ORGANIZATION = System.getProperty("organization", UUID.randomUUID().toString());
  private static final SimpleHttpServer MOCK_SERVER = new SimpleHttpServer() {

    @Override
    public void response(String requestLine, OutputStream output) {
      if (requestLine.equals("POST /accounts/login HTTP/1.1")) {
        writeJsonResponse(output, getResource("data/login.json"));
      } else if (String.format("GET /accounts/api/organizations/%s/environments HTTP/1.1", ORGANIZATION).equals(requestLine)) {
        writeJsonResponse(output, getResource("data/environments.json").replace("%%organizationId%%", ORGANIZATION)
            .replace("%%name%%", ENVIRONMENT));
      } else if ("GET /cloudhub/api/v2/applications HTTP/1.1".equals(requestLine)) {
        writeJsonResponse(output, getResource("data/get-applications.json"), 4096);
      } else {
        writeNotFoundResponse(output);
      }
    }
  };
  private static Thread SERVER_THREAD;
  private static final String BASE_URI =
      UriBuilder.fromUri("").scheme("http").host("localhost").port(MOCK_SERVER.getPort()).build().toString();

  private static String getResource(String resource) {
    try {
      return IOUtils
          .toString(Objects.requireNonNull(CloudHubClientSimpleTestCase.class.getClassLoader().getResourceAsStream(resource)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private CloudHubClient cloudHubClient;

  @BeforeAll
  static void beforeAll() {
    CLOUDHUB_DEPLOYMENT.setUri(BASE_URI);
    CLOUDHUB_DEPLOYMENT.setUsername(USERNAME);
    CLOUDHUB_DEPLOYMENT.setPassword(PASSWORD);
    CLOUDHUB_DEPLOYMENT.setEnvironment(ENVIRONMENT);
    CLOUDHUB_DEPLOYMENT.setBusinessGroupId(ORGANIZATION);
    Runnable runnable = MOCK_SERVER::startServer;;
    SERVER_THREAD = new Thread(runnable, "HttpServer-thread");
    SERVER_THREAD.setDaemon(true);
    SERVER_THREAD.start();
  }

  @BeforeEach
  void setup() {
    cloudHubClient = new CloudHubClient(CLOUDHUB_DEPLOYMENT, null);
  }

  @ParameterizedTest
  @ValueSource(strings = {"jdk", "apache", "http"})
  void testGetApplications(String connectorProvider) {
    System.setProperty("connectorProvider", connectorProvider);
    cloudHubClient.setReadEntityTimeout(2000L);
    if ("jdk".equals(connectorProvider)) {
      try {
        cloudHubClient.getApplications();
        fail("Should have thrown an exception");
      } catch (ProcessingException exception) {
        assertThat(exception)
            .hasMessage("org.glassfish.jersey.jdk.connector.internal.ParseException: \"Unexpected HTTP chunk header.\"");
        System.out.println("Unexpected HTTP chunk header");
      } catch (RuntimeException exception) {
        assertThat(exception)
            .hasMessage("java.util.concurrent.TimeoutException")
            .cause()
            .isExactlyInstanceOf(TimeoutException.class);
        System.out.println("TimeoutException");
      }
    } else {
      List<Application> applications = cloudHubClient.getApplications();
      assertThat(applications).hasSize(27);
    }
  }

  @AfterAll
  static void afterAll() {
    MOCK_SERVER.stopServer();
    SERVER_THREAD.stop();
  }
}

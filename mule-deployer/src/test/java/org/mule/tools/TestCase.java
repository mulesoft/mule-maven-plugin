/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mule.tools.client.model.TargetType;
import org.mule.tools.utils.DeployerLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mule.tools.client.authentication.AuthenticationServiceClient.ENVIRONMENTS;
import static org.mule.tools.client.authentication.AuthenticationServiceClient.LOGIN;
import static org.mule.tools.client.authentication.AuthenticationServiceClient.ME;

public class TestCase {

  protected static final String APPLICATION_FILE_NAME = "rick1-1.0.0-SNAPSHOT-mule-application-light-package.jar";
  protected static final File APPLICATION_FILE;
  protected static final MustacheFactory MUSTACHE = new DefaultMustacheFactory();
  protected static final String GET = "GET";
  protected static final String POST = "POST";
  protected static final String PATCH = "PATCH";
  protected static final String DELETE = "DELETE";

  protected static final String USERNAME = UUID.randomUUID().toString();
  protected static final String PASSWORD = UUID.randomUUID().toString();
  protected static final Logger LOGGER = LoggerFactory.getLogger(TestCase.class);
  protected static final String APACHE_5 = "apache";
  protected static final String HTTP_URL = "http";
  protected static final String JDK = "jdk";
  protected static final String ORGANIZATION_ID = "83c9625f-9372-4408-8ba0-de7980a04fae";
  protected static final String ORGANIZATION_NAME = "SALESFORCE";
  protected static final String ENVIRONMENT = "Sandbox";
  protected static final DeployerLog DEPLOYER_LOG = new DeployerLog() {

    @Override
    public void info(String s) {
      System.out.println(s);
      LOGGER.info(s);
    }

    @Override
    public void error(String s) {
      System.out.println(s);
      LOGGER.error(s);
    }

    @Override
    public void warn(String s) {
      System.out.println(s);
      LOGGER.warn(s);
    }

    @Override
    public void debug(String s) {
      System.out.println(s);
      LOGGER.debug(s);
    }

    @Override
    public void error(String s, Throwable e) {
      System.out.println(s);
      LOGGER.error(s, e);
    }

    @Override
    public boolean isDebugEnabled() {
      return true;
    }
  };
  protected static ClientAndServer SERVER;

  static {
    try {
      APPLICATION_FILE =
          new File(Objects.requireNonNull(TestCase.class.getClassLoader().getResource(APPLICATION_FILE_NAME)).toURI().getPath());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected static Stream<Arguments> connectors() {
    return Stream.of(Arguments.of(APACHE_5), Arguments.of(HTTP_URL), Arguments.of(JDK), Arguments.of((String) null));
  }

  protected static Stream<Arguments> connectorsWithTargetType() {
    return connectors()
        .flatMap(arguments -> Stream.of(TargetType.values()).map(targetType -> Arguments.of(arguments.get()[0], targetType)));
  }

  @BeforeAll
  protected static void startServer() {
    SERVER = ClientAndServer.startClientAndServer();
  }

  @AfterAll
  protected static void stopServer() {
    SERVER.stop();
  }

  protected static String getURI() {
    return String.format("http://127.0.0.1:%d", SERVER.getLocalPort());
  }

  protected static String readFile(String file) {
    try {
      return IOUtils.toString(Objects.requireNonNull(TestCase.class.getResource("/" + file)), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static String template(String file, Map<String, Object> data) {
    Writer writer = new StringWriter();
    MUSTACHE.compile(file).execute(writer, data);
    return writer.toString();
  }

  protected static void clearRequest(String method, String path) {
    SERVER.clear(HttpRequest.request(path).withMethod(method));
  }

  private static void setRequest(String method, String path, HttpResponse httpResponse) {
    HttpRequest httpRequest = HttpRequest.request(path).withMethod(method);
    SERVER.clear(httpRequest);
    SERVER.when(httpRequest).respond(httpResponse);
  }

  protected static void setStringResponse(String method, String value, String path, String contentType, int statusCode) {
    HttpResponse httpResponse = HttpResponse.response().withStatusCode(statusCode);
    if (contentType != null) {
      httpResponse
          .withHeader("Content-Type", contentType)
          .withBody(value);
    }

    setRequest(method, path, httpResponse);
  }

  protected static void setStringResponse(String method, String value, String path, String contentType) {
    setStringResponse(method, value, path, contentType, 200);
  }

  protected static void setStringResponse(String method, String value, String path) {
    setStringResponse(method, value, path, "application/json");
  }

  protected static void setStringResponse(String method, String value, String path, int statusCode) {
    setStringResponse(method, value, path, "application/json", statusCode);
  }

  protected static void setVoidResponse(String method, String path, int statusCode) {
    setStringResponse(method, null, path, "application/json", statusCode);
  }

  protected static void setVoidResponse(String method, String path) {
    setVoidResponse(method, path, 200);
  }

  protected static void setTemplateResponse(String method, String file, String path, Map<String, Object> data) {
    setStringResponse(method, template(file, data), path);
  }

  protected static void setResponse(String method, String file, String path, String contentType) {
    setStringResponse(method, readFile(file), path, contentType);
  }

  protected static void setResponse(String method, String file, String path) {
    setStringResponse(method, readFile(file), path);
  }

  protected static void setupConnector(String connector) {
    if (connector == null) {
      System.clearProperty("connectorProvider");
    } else {
      System.getProperty("connectorProvider", connector);
    }
  }

  protected static void serverSetup() {
    SERVER.reset();
    // LOGIN
    setResponse(POST, "data/login.json", LOGIN);
    setResponse(GET, "data/me.json", ME, "plain/text");
    setResponse(GET, String.format("data/%s-environments.json", ORGANIZATION_ID), String.format(ENVIRONMENTS, ".+"));
  }
}

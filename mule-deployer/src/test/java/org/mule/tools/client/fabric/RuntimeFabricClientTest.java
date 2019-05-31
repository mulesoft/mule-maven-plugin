/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.fabric;

import com.google.common.net.MediaType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mule.tools.client.fabric.model.DeploymentGenericResponse;
import org.mule.tools.client.fabric.model.Deployments;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mule.tools.client.fabric.RuntimeFabricClient.DEPLOYMENTS_PATH;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

public class RuntimeFabricClientTest {

  public static final String BASE_URI = "http://localhost:";
  public static final String GET = "GET";
  public static final String FAKE_ENV = "fakeEnv";
  public static final String FAKE_USER = "fakeUser";
  public static final String FAKE_PASSWORD = "fakePassword";
  public static final int DEFAULT_PORT = 0;
  public static final String DEPLOYMENTS_JSON = "deployments.json";
  private ClientAndServer mockServer;

  private static final String ORG_ID = "abcdef";
  private static final String ENV_ID = "ghijkl";
  private int port = DEFAULT_PORT;

  @Before
  public void startServer() {
    port = getFreePort();
    mockServer = startClientAndServer(port);
  }

  @After
  public void stopServer() {
    mockServer.stop();
  }

  @Test
  public void getDeployments() throws IOException {
    File deploymentsJson = new File(getClass().getClassLoader().getResource(
                                                                            DEPLOYMENTS_JSON)
        .getFile());
    List<String> contents = Files.readAllLines(deploymentsJson.toPath());

    mockServer.when(request().withMethod(GET).withPath(format(DEPLOYMENTS_PATH, ORG_ID, ENV_ID)))
        .respond(response().withStatusCode(200).withBody(String.join(System.lineSeparator(), contents), MediaType.JSON_UTF_8));

    RuntimeFabricClient client = buildClientSpy();

    Deployments deployments = client.getDeployments();

    Set<String> verifiedIds = newHashSet("73894e6e-d9c5-11e6-bf27-cec0c932ce01", "73894e6e-d9c5-11e6-bf27-cec0c932ce02");

    for (DeploymentGenericResponse response : deployments.items) {
      verifiedIds.remove(response.id);
    }

    assertThat("Verified ids should be empty", verifiedIds.isEmpty());
  }

  private RuntimeFabricClient buildClientSpy() {
    RuntimeFabricDeployment deployment = buildDeployment();
    RuntimeFabricClient client = spy(new RuntimeFabricClient(deployment, mock(DeployerLog.class)));
    doReturn(ORG_ID).when(client).getOrgId();
    doReturn(ENV_ID).when(client).getEnvId();
    doNothing().when(client).initialize();
    return client;
  }

  private RuntimeFabricDeployment buildDeployment() {
    RuntimeFabricDeployment deployment = new RuntimeFabricDeployment();
    deployment.setEnvironment(FAKE_ENV);
    deployment.setUsername(FAKE_USER);
    deployment.setPassword(FAKE_PASSWORD);
    deployment.setUri(BASE_URI + port);
    return deployment;
  }

  public int getFreePort() {
    try (ServerSocket socket = new ServerSocket(DEFAULT_PORT)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return DEFAULT_PORT;
  }
}


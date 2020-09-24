/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.exchange;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.mule.tools.client.authentication.model.Credentials;
import org.mule.tools.client.arm.model.AuthorizationResponse;

import javax.ws.rs.core.Response;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

@Ignore
public class ExchangeClientTest {

  private static final String EXCHANGE_REPO_URI =
      "https://maven.anypoint.mulesoft.com/api/v1/organizations/38594819-c30d-4e23-8d65-b1234d7e1eee/maven";

  private static final String USERNAME = "mulesoft";
  private static final String PASSWORD = "1234";

  private static final String GROUP_ID = "1234-5432-1324-aaaa";

  private static final String BEARER_TOKEN = "aaaaa-bbbbb";
  private static final Credentials CREDENTIALS = new Credentials(USERNAME, PASSWORD);
  private static final ExchangeRepositoryMetadata METADATA =
      new ExchangeRepositoryMetadata(CREDENTIALS, EXCHANGE_REPO_URI, new ArrayList<>());

  private ExchangeClient client;
  private ExchangeClient clientSpy;

  @Before
  public void setUp() {
    client = new ExchangeClient(METADATA);
    clientSpy = spy(client);
  }

  @Test
  public void test() {
    client = new ExchangeClient(METADATA);
    client.getGeneratedGroupId();
  }

  //  @Test
  //  public void getBearerTokenTest() {
  //    Response reponseMock = mock(Response.class);
  //    doReturn(reponseMock).when(clientSpy).loginToExchange(any());
  //    AuthorizationResponse authorizationResponse = new AuthorizationResponse();
  //    authorizationResponse.access_token = BEARER_TOKEN;
  //    doReturn(authorizationResponse).when(clientSpy).readAuthorizationResponse(reponseMock);
  //
  //    assertThat("Bearer token is not the expected", clientSpy.getBearerToken(), equalTo(BEARER_TOKEN));
  //  }
  //
  //  @Test
  //  public void getGeneratedGroupIdTest() {
  //    doReturn(BEARER_TOKEN).when(clientSpy).getBearerToken();
  //    Response reponseMock = mock(Response.class);
  //    doReturn(reponseMock).when(clientSpy).getBusinessGroup();
  //    doNothing().when(clientSpy).validateStatusSuccess(reponseMock);
  //    doReturn(GROUP_ID).when(clientSpy).readGroupId(reponseMock);
  //
  //    assertThat("Generated group id is not the expected", clientSpy.getGeneratedGroupId(), equalTo(GROUP_ID));
  //  }
}

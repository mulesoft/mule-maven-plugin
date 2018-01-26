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

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.client.authentication.AuthenticationServiceClient.AUTHORIZATION_HEADER;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import org.mule.tools.api.validation.exchange.model.Group;
import org.mule.tools.client.core.AbstractClient;
import org.mule.tools.client.authentication.AuthenticationServiceClient;
import org.mule.tools.client.authentication.model.Credentials;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This client knows how to login to Exchange and to get the required groupId for the application.
 */
// TODO make this extend of a generic client
public class ExchangeClient extends AbstractClient {

  private static final String GROUPS_PATH = "exchange/api/v1/organizations/%s/groups";

  private String bearerToken;
  private AuthenticationServiceClient authenticationServiceClient;

  private ExchangeRepositoryMetadata metadata;

  public ExchangeClient(ExchangeRepositoryMetadata metadata) {
    checkArgument(metadata != null, "The metadata must not be null");
    this.metadata = metadata;
    this.authenticationServiceClient = new AuthenticationServiceClient(metadata.getBaseUri());
  }

  protected void init() {}

  public String getGeneratedGroupId() {
    getBearerToken(metadata.getCredentials());

    Response response = get(metadata.getBaseUri(), String.format(GROUPS_PATH, metadata.getOrganizationId()));

    checkResponseStatus(response);

    Type listType = new TypeToken<ArrayList<Group>>() {}.getType();
    List<Group> groupList = new Gson().fromJson(response.readEntity(String.class), listType);

    return groupList.get(0).getGroupId();
  }

  @Override
  protected void configureRequest(Invocation.Builder builder) {
    if (bearerToken != null) {
      builder.header(AUTHORIZATION_HEADER, "bearer " + bearerToken);
    }
  }

  private String getBearerToken(Credentials credentials) {
    if (StringUtils.isBlank(bearerToken)) {
      bearerToken = authenticationServiceClient.getBearerToken(credentials);
    }
    return bearerToken;
  }

}

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

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.tools.client.agent.AbstractClient;
import org.mule.tools.client.arm.model.AuthorizationResponse;
import org.mule.tools.client.exception.ClientException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;

/**
 * This client knows how to login to Exchange and to get the required groupId for the application.
 */
public class ExchangeClient extends AbstractClient {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private final String GROUPS_PATH = "exchange/api/v1/organizations/%s/groups";
  private final ExchangeRepositoryMetadata metadata;
  private String bearerToken;

  public ExchangeClient(ExchangeRepositoryMetadata metadata) {
    this.metadata = metadata;
  }

  public String getGeneratedGroupId() {
    bearerToken = getBearerToken();
    Response response = getBusinessGroup();
    validateStatusSuccess(response);
    return readGroupId(response);
  }

  protected String readGroupId(Response response) {
    JSONObject jsonResponse = new JSONArray(response.readEntity(String.class)).getJSONObject(0);
    return jsonResponse.getString("groupId");
  }

  protected String getBearerToken() {
    Entity<String> json = Entity.json(new Gson().toJson(metadata.getCredentials()));
    Response response = loginToExchange(json);
    AuthorizationResponse authorizationResponse = readAuthorizationResponse(response);
    return authorizationResponse.access_token;
  }

  protected Response loginToExchange(Entity<String> json) {
    Response response = post(metadata.getBaseUri(), LOGIN, json);
    validateStatusSuccess(response);
    return response;
  }

  protected AuthorizationResponse readAuthorizationResponse(Response response) {
    return response.readEntity(AuthorizationResponse.class);
  }

  protected void validateStatusSuccess(Response response) {
    if (familyOf(response.getStatus()) != SUCCESSFUL) {
      throw new ClientException(response);
    }
  }

  @Override
  protected void configureRequest(Invocation.Builder builder) {
    if (bearerToken != null) {
      builder.header(AUTHORIZATION_HEADER, "bearer " + bearerToken);
    }
  }

  public Response getBusinessGroup() {
    return get(metadata.getBaseUri(), String.format(GROUPS_PATH, metadata.getOrganizationId()));
  }
}

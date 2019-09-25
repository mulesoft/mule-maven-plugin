/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.authentication;

import static com.google.common.base.Preconditions.checkArgument;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;

import org.mule.tools.client.core.AbstractClient;
import org.mule.tools.client.arm.model.AuthorizationResponse;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Environments;
import org.mule.tools.client.arm.model.Organization;
import org.mule.tools.client.arm.model.UserInfo;
import org.mule.tools.client.authentication.model.Credentials;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class AuthenticationServiceClient extends AbstractClient {

  public static final String AUTHORIZATION_HEADER = "Authorization";

  public static final String ANYPOINT_SEESION_EXTEND = "x-anypoint-session-extend";

  public static final String LOGIN = "/accounts/login";

  public static final String BASE = "/accounts/api";


  public static final String ME = BASE + "/me";
  public static final String ORGANIZATIONS = BASE + "/organizations";
  public static final String ENVIRONMENTS = ORGANIZATIONS + "/%s/environments";

  private String baseUri;

  // TODO review saveState
  private Boolean saveState;
  private String bearerToken;

  public AuthenticationServiceClient(String baseUri) {
    this(baseUri, false);
  }

  public AuthenticationServiceClient(String baseUri, Boolean saveState) {
    checkArgument(StringUtils.isNotBlank(baseUri), "The baseUri must not be null nor empty");
    this.baseUri = baseUri;
    this.saveState = saveState;
  }

  protected void init() {
    // Do nothing
  }

  // TODO find a way to just login and save state
  public String getBearerToken(Credentials credentials) {
    AuthorizationResponse authorizationResponse = login(credentials);

    if (saveState) {
      bearerToken = authorizationResponse.access_token;
    }

    return authorizationResponse.access_token;
  }

  public UserInfo getMe() {
    UserInfo userInfo = get(baseUri, ME, UserInfo.class);

    return userInfo;
  }

  // TODO review this API impl
  public List<Organization> getOrganizations() {
    Response response = get(baseUri, ORGANIZATIONS);

    Type listType = new TypeToken<ArrayList<Organization>>() {}.getType();
    List<Organization> organizationList = new Gson().fromJson(response.readEntity(String.class), listType);

    return organizationList;
  }

  public List<Environment> getEnvironments(String organizationId) {
    Environments environments = get(baseUri, String.format(ENVIRONMENTS, organizationId), Environments.class);

    return Arrays.asList(environments.data);
  }

  protected void configureRequest(Invocation.Builder builder) {
    if (bearerToken != null) {
      builder.header(AUTHORIZATION_HEADER, "bearer " + bearerToken);
    }
  }

  private AuthorizationResponse login(Credentials credentials) {
    Entity<String> credentialsEntity = Entity.json(new Gson().toJson(credentials));

    Response response = post(baseUri, LOGIN, credentialsEntity);

    checkResponseStatus(response);

    return response.readEntity(AuthorizationResponse.class);
  }

}

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;

import org.mule.tools.client.arm.model.AuthorizationResponse;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Environments;
import org.mule.tools.client.arm.model.UserInfo;

import java.util.ArrayList;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.tools.client.exception.ClientException;

public abstract class AbstractMuleClient extends AbstractClient {

  private static final String ME = "/accounts/api/me";
  private static final String ENVIRONMENTS = "/accounts/api/organizations/%s/environments";

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
  private static final String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";

  protected String uri;
  private String username;
  private String password;
  private String environment;
  private final String businessGroup;

  private String bearerToken;
  private String envId;
  private String orgId;

  public AbstractMuleClient(String uri, Log log, String username, String password, String environment, String businessGroup) {
    super(log);
    this.uri = uri;
    this.username = username;
    this.password = password;
    this.environment = environment;
    this.businessGroup = businessGroup;
  }

  public void init() {
    bearerToken = getBearerToken(username, password);
    orgId = getOrgId();
    envId = findEnvironmentByName(environment).id;
  }

  private String getBearerToken(String username, String password) {
    Entity<String> json = Entity.json("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}");
    Response response = post(uri, LOGIN, json);
    validateStatusSuccess(response);
    AuthorizationResponse authorizationResponse = response.readEntity(AuthorizationResponse.class);
    return authorizationResponse.access_token;
  }

  protected void validateStatusSuccess(Response response) {
    if (familyOf(response.getStatus()) != SUCCESSFUL) {
      throw new ClientException(response);
    }
  }

  public String getOrgId() {
    return findBusinessGroup();
  }

  public Environment findEnvironmentByName(String name) {
    Environments response = get(uri, String.format(ENVIRONMENTS, orgId), Environments.class);

    for (int i = 0; i < response.data.length; i++) {
      if (name.equals(response.data[i].name)) {
        return response.data[i];
      }
    }
    throw new RuntimeException("Couldn't find environment named [" + name + "]");
  }

  @Override
  protected void configureRequest(Invocation.Builder builder) {
    if (bearerToken != null) {
      builder.header(AUTHORIZATION_HEADER, "bearer " + bearerToken);
    }

    if (envId != null && orgId != null) {
      builder.header(ENV_ID_HEADER, envId);
      builder.header(ORG_ID_HEADER, orgId);
    }
  }

  private JSONObject getHierarchy() {
    UserInfo response = get(uri, ME, UserInfo.class);
    String rootOrgId = response.user.organization.id;
    return new JSONObject(get(uri, "accounts/api/organizations/" + rootOrgId + "/hierarchy", String.class));
  }

  public String findBusinessGroup() {
    String currentOrgId = null;
    String[] groups = createBusinessGroupPath();
    JSONObject json = getHierarchy(); // Using JSON parsing because Jersey unmarshalling fails to create all business groups
    JSONArray subOrganizations = (JSONArray) json.get("subOrganizations");
    if (groups.length == 0) {
      return (String) json.get("id");
    }
    for (int group = 0; group < groups.length; group++) {
      for (int organization = 0; organization < subOrganizations.length(); organization++) {
        JSONObject jsonObject = (JSONObject) subOrganizations.get(organization);
        if (jsonObject.get("name").equals(groups[group])) {
          currentOrgId = (String) jsonObject.get("id");
          subOrganizations = (JSONArray) jsonObject.get("subOrganizations");
        }
      }
    }
    if (currentOrgId == null) {
      throw new ArrayIndexOutOfBoundsException("Cannot find business group.");
    }
    return currentOrgId;
  }

  protected String[] createBusinessGroupPath() {
    if (StringUtils.isEmpty(businessGroup)) {
      return new String[0];
    }
    ArrayList<String> groups = new ArrayList<>();
    String group = "";
    int i = 0;
    for (; i < businessGroup.length() - 1; i++) {
      if (businessGroup.charAt(i) == '\\') {
        if (businessGroup.charAt(i + 1) == '\\') // Double backslash maps to business group with one backslash
        {
          group = group + "\\";
          i++; // For two backslashes we continue with the next character
        } else // Single backslash starts a new business group
        {
          groups.add(group);
          group = "";
        }
      } else // Non backslash characters are mapped to the group
      {
        group = group + businessGroup.charAt(i);
      }
    }
    if (i < businessGroup.length()) // Do not end with backslash
    {
      group = group + businessGroup.charAt(businessGroup.length() - 1);
    }
    groups.add(group);
    return groups.toArray(new String[0]);
  }


}

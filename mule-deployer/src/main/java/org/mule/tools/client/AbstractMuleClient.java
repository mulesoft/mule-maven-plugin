/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mule.tools.client.authentication.AuthenticationServiceClient.AUTHORIZATION_HEADER;
import static org.mule.tools.client.authentication.AuthenticationServiceClient.ANYPOINT_SEESION_EXTEND;

import java.util.*;

import javax.ws.rs.client.Invocation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.mule.tools.client.core.AbstractClient;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Environments;
import org.mule.tools.client.arm.model.Organization;
import org.mule.tools.client.arm.model.User;
import org.mule.tools.client.arm.model.UserInfo;
import org.mule.tools.client.authentication.AuthenticationServiceClient;
import org.mule.tools.client.authentication.model.AnypointCredential;
import org.mule.tools.client.authentication.model.AnypointToken;
import org.mule.tools.client.authentication.model.Credentials;
import org.mule.tools.model.anypoint.AnypointDeployment;
import org.mule.tools.utils.DeployerLog;


public abstract class AbstractMuleClient extends AbstractClient {

  public static final String DEFAULT_BASE_URL = "https://anypoint.mulesoft.com";

  private static final String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
  private static final String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";

  private static final String ME = "/accounts/api/me";
  private static final String ENVIRONMENTS = "/accounts/api/organizations/%s/environments";
  public static final String ORGANIZATION = "organization";
  public static final String SUB_ORGANIZATION_IDS = "subOrganizationIds";
  public static final String NAME = "name";
  public static final String USER = "user";
  public static final String ID = "id";

  public static final String UNAUTHORIZED = "unauthorized";

  protected String baseUri;

  private String bearerToken;
  private AnypointCredential credentials;
  protected AuthenticationServiceClient authenticationServiceClient;

  // TODO MMP-302
  private String envId;
  private String environmentName;

  private String orgId;
  private String businessGroupName;
  private String businessGroupId;

  public AbstractMuleClient(AnypointDeployment anypointDeployment, DeployerLog log) {
    super(log);
    this.baseUri = anypointDeployment.getUri();

    if (!isEmpty(anypointDeployment.getAuthToken())) {
      this.credentials = new AnypointToken(anypointDeployment.getAuthToken());
    } else {
      this.credentials = new Credentials(anypointDeployment.getUsername(), anypointDeployment.getPassword());
    }

    this.authenticationServiceClient = new AuthenticationServiceClient(baseUri);

    this.environmentName = anypointDeployment.getEnvironment();
    this.businessGroupName = anypointDeployment.getBusinessGroup();

    if (anypointDeployment.getBusinessGroupId() != null) {
      this.businessGroupId = anypointDeployment.getBusinessGroupId();
    }
  }

  public AbstractMuleClient(DeployerLog log) {
    super(log);
  }

  public void init() {
    bearerToken = getBearerToken(credentials);
    orgId = businessGroupId != null ? businessGroupId : getOrgId();
    envId = findEnvironmentByName(environmentName).id;
  }

  public UserInfo getMe() {
    String userInfoJsonString = get(baseUri, ME, String.class);
    if (userInfoJsonString.equalsIgnoreCase(UNAUTHORIZED)) {
      StringBuilder message = new StringBuilder();
      message.append("Unauthorized Access. Please verify that authToken is valid.");
      throw new RuntimeException(message.toString());
    }
    JsonObject userInfoJson = (JsonObject) new JsonParser().parse(userInfoJsonString);
    Organization organization = buildOrganization(userInfoJson);
    User user = new User();
    user.organization = organization;
    UserInfo userInfo = new UserInfo();
    userInfo.user = user;
    return userInfo;
  }

  protected Organization buildOrganization(JsonObject userInfoJson) {
    Map<String, Organization> organizationsIds = new HashMap<>();
    Map<String, List<String>> organizationChildren = new HashMap<>();
    buildOrganizationsMap(userInfoJson, organizationsIds, organizationChildren);
    String root = getRootOrganization(userInfoJson);
    return recurse(root, organizationsIds, organizationChildren);
  }

  private Organization recurse(String root, Map<String, Organization> organizationsIds,
                               Map<String, List<String>> organizationChildren) {
    Organization organization = organizationsIds.get(root);
    for (String child : organizationChildren.get(root)) {
      organization.subOrganizations.add(recurse(child, organizationsIds, organizationChildren));
    }
    return organization;
  }

  private String getRootOrganization(JsonObject userInfoJson) {
    if (userInfoJson != null && userInfoJson.has(USER)) {
      JsonObject userJson = (JsonObject) userInfoJson.get(USER);
      if (userJson != null && userJson.has(ORGANIZATION)) {
        JsonObject organizationJson = userJson.getAsJsonObject(ORGANIZATION);
        if (organizationJson.has(NAME)) {
          return organizationJson.get(ID).getAsString();
        }
      }
    }
    throw new IllegalStateException("Cannot find root organization");
  }

  /**
   * Maps every organization id to a organization object Maps every organization id to its children organization ids
   *
   * @param userInfoJson
   * @param organizationsIds
   * @param organizationChildren
   */
  private void buildOrganizationsMap(JsonObject userInfoJson,
                                     Map<String, Organization> organizationsIds,
                                     Map<String, List<String>> organizationChildren) {
    if (userInfoJson != null && userInfoJson.has(USER)) {
      JsonObject userJson = (JsonObject) userInfoJson.get(USER);
      JsonArray organizations = getAllOrganizations(userJson);
      for (JsonElement orgJson : organizations) {
        if (orgJson.isJsonObject()) {
          Organization org = new Organization();
          org.id = ((JsonObject) orgJson).get(ID).getAsString();
          org.name = ((JsonObject) orgJson).get(NAME).getAsString();
          org.subOrganizations = new ArrayList<>();
          List<String> childrenIds = getChildren(orgJson);
          organizationsIds.put(org.id, org);
          organizationChildren.put(org.id, childrenIds);
        }
      }
    }
  }

  private List<String> getChildren(JsonElement orgJson) {
    JsonArray children = ((JsonObject) orgJson).get("subOrganizationIds").getAsJsonArray();
    List<String> ids = new ArrayList<>();
    for (int i = 0; i < children.size(); i++) {
      ids.add(children.get(i).getAsString());
    }
    return ids;
  }

  private JsonArray getAllOrganizations(JsonObject userJson) {
    JsonArray organizations = new JsonArray();
    if (userJson.has("memberOfOrganizations")) {
      organizations.addAll(userJson.get("memberOfOrganizations").getAsJsonArray());
    }
    if (userJson.has("contributorOfOrganizations")) {
      organizations.addAll(userJson.get("contributorOfOrganizations").getAsJsonArray());
    }
    return organizations;
  }

  public String getOrgId() {
    return businessGroupId != null ? businessGroupId : getBusinessGroupIdByBusinessGroupPath();
  }

  public String getEnvId() {
    return envId;
  }

  // TODO use AuthenticationServiceClient
  public Environment findEnvironmentByName(String name) {
    Environments response = getEnvironments();
    if (response == null || response.data == null) {
      StringBuilder message = new StringBuilder();
      message.append("Please check whether you have the access rights to this business group.");
      if (isEmpty(businessGroupName)) {
        message
            .append(
                    " Please set the businessGroup in the plugin configuration in case your user have access only within a business unit.");
      }
      throw new RuntimeException(message.toString());
    }
    for (int i = 0; i < response.data.length; i++) {
      if (name.equals(response.data[i].name)) {
        return response.data[i];
      }
    }
    throw new RuntimeException("Couldn't find environmentName named [" + name + "]");
  }

  // TODO find a better way to do this
  @Override
  protected void configureRequest(Invocation.Builder builder) {
    if (bearerToken != null) {
      builder.header(AUTHORIZATION_HEADER, "bearer " + bearerToken);
      builder.header(ANYPOINT_SEESION_EXTEND, true);
    }

    if (envId != null && orgId != null) {
      builder.header(ENV_ID_HEADER, envId);
      builder.header(ORG_ID_HEADER, orgId);
    }
  }

  protected String[] createBusinessGroupPath() {
    if (isEmpty(businessGroupName)) {
      return new String[0];
    }

    ArrayList<String> groups = new ArrayList<>();

    String group = "";
    int i = 0;
    for (; i < businessGroupName.length() - 1; i++) {
      if (businessGroupName.charAt(i) == '\\') {
        // Double backslash maps to business group with one backslash
        if (businessGroupName.charAt(i + 1) == '\\') {
          // For two backslashes we continue with the next character
          group = group + "\\";
          i++;
        } else {
          // Single backslash starts a new business group
          groups.add(group);
          group = "";
        }
      } else {
        // Non backslash characters are mapped to the group
        group = group + businessGroupName.charAt(i);
      }
    }
    // Do not end with backslash
    if (i < businessGroupName.length()) {
      group = group + businessGroupName.charAt(businessGroupName.length() - 1);
    }
    groups.add(group);
    return groups.toArray(new String[0]);
  }

  public String getBusinessGroupIdByBusinessGroupPath() {
    String currentOrgId = null;

    Organization organizationHierarchy = getMe().user.organization;
    if (organizationHierarchy.subOrganizations.isEmpty() ||
        organizationHierarchy.name.equals(businessGroupName)) {
      return organizationHierarchy.id;
    }
    List<Organization> subOrganizations = organizationHierarchy.subOrganizations;

    String[] groups = createBusinessGroupPath();
    if (groups.length == 0) {
      currentOrgId = organizationHierarchy.id;
    } else {
      for (int group = 0; group < groups.length; group++) {
        String groupName = groups[group];

        for (Organization o : subOrganizations) {
          if (o.name.equals(groupName)) {
            currentOrgId = o.id;
            subOrganizations = o.subOrganizations;
            break;
          }
        }
      }
    }

    if (currentOrgId == null) {
      throw new ArrayIndexOutOfBoundsException("Cannot find business group.");
    }

    return currentOrgId;
  }

  private String getBearerToken(AnypointCredential credentials) {
    if (isBlank(bearerToken)) {
      switch (credentials.credentialType()) {
        case user:
          Credentials creds = (Credentials) credentials;
          bearerToken = authenticationServiceClient.getBearerToken(creds);
          break;
        case token:
          bearerToken = ((AnypointToken) credentials).getToken();
          break;
      }
    }

    return bearerToken;
  }

  public Environments getEnvironments() {
    return get(baseUri, String.format(ENVIRONMENTS, orgId), Environments.class);
  }

  public Set<String> getSuborganizationIds(JsonObject organizationJson) {
    Set<String> suborganizationIds = new HashSet<>();
    JsonArray subOrganizationIds = organizationJson.get("subOrganizationIds").getAsJsonArray();
    if (subOrganizationIds != null) {
      for (JsonElement id : subOrganizationIds) {
        suborganizationIds.add(id.getAsString());
      }
    }
    return suborganizationIds;
  }
}

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

import static org.mule.tools.client.authentication.AuthenticationServiceClient.AUTHORIZATION_HEADER;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Invocation;

import org.apache.commons.lang3.StringUtils;

import org.mule.tools.client.agent.AbstractClient;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Environments;
import org.mule.tools.client.arm.model.Organization;
import org.mule.tools.client.arm.model.UserInfo;
import org.mule.tools.client.authentication.AuthenticationServiceClient;
import org.mule.tools.client.authentication.model.Credentials;
import org.mule.tools.model.anypoint.AnypointDeployment;
import org.mule.tools.utils.DeployerLog;

import com.google.gson.Gson;

public abstract class AbstractMuleClient extends AbstractClient {

  public static final String DEFAULT_BASE_URL = "https://anypoint.mulesoft.com";

  private static final String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
  private static final String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";

  private static final String ME = "/accounts/api/me";
  private static final String ENVIRONMENTS = "/accounts/api/organizations/%s/environments";

  protected String baseUri;

  private String bearerToken;
  private Credentials credentials;
  protected AuthenticationServiceClient authenticationServiceClient;

  // TODO MMP-302
  private String envId;
  private String environmentName;

  private String orgId;
  private String businessGroupName;

  public AbstractMuleClient(AnypointDeployment anypointDeployment, DeployerLog log) {
    super(log);
    this.baseUri = anypointDeployment.getUri();

    this.credentials = new Credentials(anypointDeployment.getUsername(), anypointDeployment.getPassword());

    this.authenticationServiceClient = new AuthenticationServiceClient(baseUri);

    this.environmentName = anypointDeployment.getEnvironment();
    this.businessGroupName = anypointDeployment.getBusinessGroup();
  }

  public void init() {
    bearerToken = getBearerToken(credentials);

    orgId = getOrgId();
    envId = findEnvironmentByName(environmentName).id;
  }

  public UserInfo getMe() {
    UserInfo userInfo = get(baseUri, ME, UserInfo.class);
    return userInfo;
  }

  public String getOrgId() {
    return getBusinessGroupIdByBusinessGroupPath();
  }

  // TODO use AuthenticationServiceClient
  public Environment findEnvironmentByName(String name) {
    Environments response = get(baseUri, String.format(ENVIRONMENTS, orgId), Environments.class);

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
    }

    if (envId != null && orgId != null) {
      builder.header(ENV_ID_HEADER, envId);
      builder.header(ORG_ID_HEADER, orgId);
    }
  }

  protected String[] createBusinessGroupPath() {
    if (StringUtils.isEmpty(businessGroupName)) {
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

  private String getBusinessGroupIdByBusinessGroupPath() {
    String currentOrgId = null;

    Organization organizationHierarchy = getOrganizationHierarchy();
    if (organizationHierarchy.subOrganizations.isEmpty()) {
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

  private Organization getOrganizationHierarchy() {
    String rootOrgId = getMe().user.organization.id;

    String organizationHiearchy = get(baseUri, "accounts/api/organizations/" + rootOrgId + "/hierarchy", String.class);
    Organization organization = new Gson().fromJson(organizationHiearchy, Organization.class);

    return organization;
  }

  private String getBearerToken(Credentials credentials) {
    if (StringUtils.isBlank(bearerToken)) {
      bearerToken = authenticationServiceClient.getBearerToken(credentials);
    }

    return bearerToken;
  }
}

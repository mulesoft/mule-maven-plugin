/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mule.tools.maven.plugin.mule.arm.*;

import java.util.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONObject;

public abstract class AbstractMuleApi extends AbstractApi
{

    private static final String ME = "/accounts/api/me";
    private static final String ENVIRONMENTS = "/accounts/api/organizations/%s/environments";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
    private static final String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";
    public static final String ORGANIZATION = "organization";
    public static final String SUB_ORGANIZATION_IDS = "subOrganizationIds";
    public static final String NAME = "name";
    public static final String USER = "user";
    public static final String ID = "id";

    protected String uri;
    private String username;
    private String password;
    private String environment;
    private final String businessGroup;

    private String bearerToken;
    private String envId;
    private String orgId;

    public AbstractMuleApi(String uri, Log log, String username, String password, String environment, String businessGroup)
    {
        super(log);
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.environment = environment;
        this.businessGroup = businessGroup;
    }

    public void init()
    {
        bearerToken = getBearerToken(username, password);
        orgId = getOrgId();
        envId = findEnvironmentByName(environment).id;
    }

    private String getBearerToken(String username, String password)
    {
        Entity<String> json = Entity.json("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}");
        Response response = post(uri, LOGIN, json);
        validateStatusSuccess(response);
        AuthorizationResponse authorizationResponse = response.readEntity(AuthorizationResponse.class);
        return authorizationResponse.access_token;
    }

    protected void validateStatusSuccess(Response response)
    {
        if (familyOf(response.getStatus()) != SUCCESSFUL)
        {
            throw new ApiException(response);
        }
    }

    public String getOrgId()
    {
        return getBusinessGroupIdByBusinessGroupPath();
    }

    public Environment findEnvironmentByName(String name)
    {
        Environments response = get(uri, String.format(ENVIRONMENTS, orgId), Environments.class);

        for (int i = 0 ; i < response.data.length ; i ++ )
        {
            if (name.equals(response.data[i].name))
            {
                return response.data[i];
            }
        }
        throw new RuntimeException("Couldn't find environment named [" + name + "]");
    }

    @Override
    protected void configureRequest(Invocation.Builder builder)
    {
        if (bearerToken != null)
        {
            builder.header(AUTHORIZATION_HEADER, "bearer " + bearerToken);
        }

        if (envId != null && orgId != null)
        {
            builder.header(ENV_ID_HEADER, envId);
            builder.header(ORG_ID_HEADER, orgId);
        }
    }

    public String getBusinessGroupIdByBusinessGroupPath() {
        String currentOrgId = null;

        Organization organizationHierarchy = getMe().user.organization;
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

    public UserInfo getMe() {
        String userInfoJsonString = get(uri, ME, String.class);
        JsonObject userInfoJson = (JsonObject) new JsonParser().parse(userInfoJsonString);
        Organization organization = buildOrganization(userInfoJson);
        User user = new User();
        user.organization = organization;
        UserInfo userInfo = new UserInfo();
        userInfo.user = user;
        return userInfo;
    }

    private Organization buildOrganization(JsonObject userInfoJson) {
        Organization organization = new Organization();
        if (userInfoJson != null && userInfoJson.has(USER)) {
            JsonObject userJson = (JsonObject) userInfoJson.get(USER);
            if (userJson != null && userJson.has(ORGANIZATION)) {
                JsonObject organizationJson = userJson.getAsJsonObject(ORGANIZATION);
                if (organizationJson.has(ID) && organizationJson.has(NAME)) {
                    organization.id = organizationJson.get(ID).getAsString();
                    organization.name = organizationJson.get(NAME).getAsString();
                    organization.subOrganizations = resolveSuborganizations(userJson, organizationJson);
                }
            }
        }
        return organization;
    }

    protected List<Organization> resolveSuborganizations(JsonObject userJson, JsonObject organizationJson) {
        List<Organization> suborganizations = new ArrayList<>();
        if (organizationJson.has(SUB_ORGANIZATION_IDS)) {
            Set<String> ids = getSuborganizationIds(organizationJson);
            suborganizations.addAll(resolveAllSuborganizations(ids, userJson, "memberOfOrganizations"));
            suborganizations.addAll(resolveAllSuborganizations(ids, userJson, "contributorOfOrganizations"));
        }
        return suborganizations;
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

    private Collection<? extends Organization> resolveAllSuborganizations(Set<String> ids, JsonObject userJson,
                                                                          String organizationsDefinition) {
        List<Organization> suborganizations = new ArrayList<>();
        if (userJson.has(organizationsDefinition)) {
            JsonArray subOrganizationUserIsMemberOf = userJson.get(organizationsDefinition).getAsJsonArray();
            if (subOrganizationUserIsMemberOf != null) {
                for (JsonElement org : subOrganizationUserIsMemberOf) {
                    if (org.isJsonObject()) {
                        Organization suborganization = new Organization();
                        suborganization.id = ((JsonObject) org).get(ID).getAsString();
                        if (ids.contains(suborganization.id)) {
                            suborganization.name = ((JsonObject) org).get(NAME).getAsString();
                            suborganizations.add(suborganization);
                            ids.remove(suborganization.id);
                        }
                    }
                }
            }
        }
        return suborganizations;
    }

    protected String[] createBusinessGroupPath()
    {
        if (StringUtils.isEmpty(businessGroup))
        {
            return new String[0];
        }
        ArrayList<String> groups = new ArrayList<>();
        String group = "";
        int i = 0;
        for (; i < businessGroup.length() -1; i ++)
        {
            if (businessGroup.charAt(i) == '\\')
            {
                if (businessGroup.charAt(i+1) == '\\') // Double backslash maps to business group with one backslash
                {
                    group = group + "\\";
                    i++; // For two backslashes we continue with the next character
                }
                else // Single backslash starts a new business group
                {
                    groups.add(group);
                    group = "";
                }
            }
            else // Non backslash characters are mapped to the group
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

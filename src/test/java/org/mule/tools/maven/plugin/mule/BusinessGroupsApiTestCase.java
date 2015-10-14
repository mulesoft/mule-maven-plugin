/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.tools.maven.plugin.mule.arm.AuthorizationResponse;
import org.mule.tools.maven.plugin.mule.arm.UserInfo;
import org.mule.tools.maven.plugin.mule.cloudhub.CloudhubApi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class BusinessGroupsApiTestCase
{

    private static final String URI = "https://anypoint.mulesoft.com";
    private static final String USERNAME = System.getProperty("username");
    private static final String PASSWORD = System.getProperty("password");
    private static final String ENVIRONMENT = "Production";
    private static final String REGION = "us-east-1";
    private static final String MULE_VERSION = "3.6.1";
    private static final int WORKERS = 1;
    private static final String WORKER_TYPE = "Medium";

    private static final String APP_NAME = "test-app-12345";
    private static final File APP = new File("/tmp/echo-test4.zip");
    private static final String LOGIN = "/accounts/login";
    private CloudhubApi cloudhubApi;
    private Map<String, String> properties = new HashMap();
    private String bearerToken;

    @Before
    public void setup()
    {
        bearerToken = getBearerToken("alesequeira", "ThisIsMyPass1");
    }

    private String getBearerToken(String username, String password)
    {
        Entity<String> json = Entity.json("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}");
        WebTarget target = ClientBuilder.newClient().target(URI).path(LOGIN);
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(json);
        assert (response.getStatus() == 200);
        return response.readEntity(AuthorizationResponse.class).access_token;
    }

    @Test
    public void getRootOrganization()
    {
        assertThat("93a65772-0296-4878-9673-e093d246bbef", equalTo(findBusinessGroup("")));
    }

    @Test
    public void getRootOrganizationWitEmptyPath()
    {
        assertThat("93a65772-0296-4878-9673-e093d246bbef", equalTo(findBusinessGroup("")));
    }

    @Test
    public void getFirstLevelOrganization()
    {
        assertThat("ecc0f4eb-e22c-4955-9662-6f64126df01d", equalTo(findBusinessGroup("test")));
    }

    @Test
    public void getSecondLevelOrganization()
    {
        assertThat("ca58c363-1f3b-4e8b-822b-1274aad454c9", equalTo(findBusinessGroup("test.test")));
    }

    @Test(expected = RuntimeException.class)
    public void failForInvalidOrganiztion()
    {
        findBusinessGroup("fake");
    }

    public String findBusinessGroup(String path)
    {
        String currentOrgId = null;
        String[] groups = path.split("\\.");
        JSONObject json = getHierarchy(); // Using JSON parsing because Jersey unmarshalling fails to create all business groups
        JSONArray subOrganizations = (JSONArray) json.get("subOrganizations");
        if (StringUtils.isEmpty(path) || groups.length == 0)
        {
            return (String) json.get("id");
        }
        for (int group = 0; group < groups.length; group++)
        {
            for (int organization = 0; organization < subOrganizations.length(); organization++)
            {
                JSONObject jsonObject = (JSONObject) subOrganizations.get(organization);
                if (jsonObject.get("name").equals(groups[group]))
                {
                    currentOrgId = (String) jsonObject.get("id");
                    subOrganizations = (JSONArray) jsonObject.get("subOrganizations");
                }
            }
        }
        if (currentOrgId == null)
        {
            throw new ArrayIndexOutOfBoundsException("Cannot find business group.");
        }
        return currentOrgId;
    }

    private JSONObject getHierarchy()
    {
        String orgId = getOrganizationId(bearerToken);
        WebTarget target = ClientBuilder.newClient().target(URI).path("accounts/api/organizations/" + orgId + "/hierarchy");
        return new JSONObject(target.request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "bearer " + bearerToken).get(String.class));
    }

    private String getOrganizationId(String bearerToken)
    {
        WebTarget target = ClientBuilder.newClient().target(URI).path("/accounts/api/me");
        UserInfo userInfo = target.request(MediaType.APPLICATION_JSON_TYPE).
                header("Authorization", "bearer " + bearerToken).
                get(UserInfo.class);
        assert userInfo != null;
        return userInfo.user.organization.id;
    }

}



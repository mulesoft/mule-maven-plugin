/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule;

import org.mule.tools.mule.arm.AuthorizationResponse;
import org.mule.tools.mule.arm.Environment;
import org.mule.tools.mule.arm.Environments;
import org.mule.tools.mule.arm.UserInfo;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class AbstractMuleApi
{

    private static final String ME = "/accounts/api/me";
    private static final String URI = "https://anypoint.mulesoft.com";
    private static final String LOGIN = "/accounts/login";
    private static final String ENVIRONMENTS = "/accounts/api/organizations/%s/environments";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
    private static final String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";

    private String username;
    private String password;
    private String environment;

    private String bearerToken;
    private String envId;
    private String orgId;

    public AbstractMuleApi(String username, String password, String environment)
    {
        this.username = username;
        this.password = password;
        this.environment = environment;
    }

    public void init()
    {
        bearerToken = getBearerToken(username, password);
        orgId = getOrgId();
        envId = findEnvironmentByName(environment).id;
    }


    protected MultivaluedMap<String, Object> authorizationHeader()
    {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(AUTHORIZATION_HEADER, "bearer " + bearerToken);
        headers.add(ENV_ID_HEADER, envId);
        headers.add(ORG_ID_HEADER, orgId);
        return headers;
    }

    public String getBearerToken(String username, String password)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URI).path(LOGIN);
        Entity<String> json = Entity.json("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}");
        AuthorizationResponse response = target.request(MediaType.TEXT_PLAIN).post(json, AuthorizationResponse.class);
        return response.access_token;
    }

    public String getOrgId()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URI).path(ME);
        UserInfo response = target.request(MediaType.APPLICATION_JSON_TYPE).
                header(AUTHORIZATION_HEADER, "bearer " + bearerToken).get(UserInfo.class);
        return response.user.organization.id;
    }

    public Environment findEnvironmentByName(String name)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URI).path(String.format(ENVIRONMENTS, orgId));
        Environments response = target.request(MediaType.APPLICATION_JSON_TYPE).
                header(AUTHORIZATION_HEADER, "bearer " + bearerToken).get(Environments.class);
        for (int i = 0 ; i < response.data.length ; i ++ )
        {
            if (name.equals(response.data[i].name))
            {
                return response.data[i];
            }
        }
        throw new RuntimeException("Couldn't find environment named [" + name + "]");
    }

}

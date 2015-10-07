/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;

import org.mule.tools.maven.plugin.mule.arm.AuthorizationResponse;
import org.mule.tools.maven.plugin.mule.arm.Environments;
import org.mule.tools.maven.plugin.mule.arm.UserInfo;
import org.mule.tools.maven.plugin.mule.arm.Environment;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.maven.plugin.logging.Log;

public abstract class AbstractMuleApi extends AbstractApi
{

    private static final String ME = "/accounts/api/me";
    private static final String LOGIN = "/accounts/login";
    private static final String ENVIRONMENTS = "/accounts/api/organizations/%s/environments";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
    private static final String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";

    protected String uri;
    private String username;
    private String password;
    private String environment;

    private String bearerToken;
    private String envId;
    private String orgId;

    public AbstractMuleApi(String uri, Log log, String username, String password, String environment)
    {
        super(log);
        this.uri = uri;
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
        UserInfo response = get(uri, ME, UserInfo.class);
        return response.user.organization.id;
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
}

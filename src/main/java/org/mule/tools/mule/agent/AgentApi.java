/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule.agent;

import org.mule.tools.mule.ApiException;

import java.io.File;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class AgentApi
{

    public static final String APPLICATIONS_PATH = "/mule/applications/";

    private final String uri;

    public AgentApi(String uri)
    {
        this.uri = uri;
    }

    public void deployApplication(String applicationName, File file)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri).path(APPLICATIONS_PATH + applicationName);

        Response response = target.
                request(MediaType.APPLICATION_JSON_TYPE).
                put(Entity.entity(file, MediaType.APPLICATION_OCTET_STREAM_TYPE));

        if (response.getStatus() != 202) // Created
        {
            String message = response.readEntity(String.class);
            throw new ApiException(message, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
        }
    }

    public void undeployApplication(String appName)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri).path(APPLICATIONS_PATH + appName);

        Response response = target.
                request(MediaType.APPLICATION_JSON_TYPE).
                delete();

        if (response.getStatus() != 202)
        {
            String message = response.readEntity(String.class);
            throw new ApiException(message, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
        }
    }

}

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule.agent;

import org.mule.tools.mule.ApiException;
import org.mule.util.IOUtils;
import org.mule.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;

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
    private final String trustStorePath;
    private final String trustStorePassword;
    private final String trustStoreType;

    public AgentApi(String uri, String trustStorePath, String trustStorePassword, String trustStoreType)
    {
        this.uri = uri;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.trustStoreType = trustStoreType;
    }

    public void deployApplication(String applicationName, File file)
    {
        Client client = getClient();
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
        Client client = getClient();
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

    private Client getClient()
    {
        ClientBuilder builder = ClientBuilder.newBuilder();

        if (StringUtils.isNotEmpty(trustStorePath))
        {
            KeyStore trustStore;
            try
            {
                trustStore = KeyStore.getInstance(trustStoreType);
                InputStream is = IOUtils.getResourceAsStream(trustStorePath, getClass());
                if (null == is)
                {
                    throw new RuntimeException("Cannot find trust store file " + trustStorePath);
                }
                trustStore.load(is, trustStorePassword.toCharArray());


                builder.trustStore(trustStore);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Cannot load trust store file " + trustStorePath, e);
            }
        }

        return builder.build();
    }

}

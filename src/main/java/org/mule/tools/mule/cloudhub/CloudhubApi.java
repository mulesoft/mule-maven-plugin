/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule.cloudhub;

import org.mule.tools.mule.AbstractMuleApi;
import org.mule.tools.mule.ApiException;

import java.io.File;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

public class CloudhubApi extends AbstractMuleApi
{

    public static final String URI = "https://anypoint.mulesoft.com";
    public static final String APPLICATIONS_PATH = "/cloudhub/api/applications";
    public static final String APPLICATION_UPDATE_PATH = "/cloudhub/api/v2/applications/%s";
    public static final String APPLICATIONS_FILES_PATH = "/cloudhub/api/v2/applications/%s/files";
    public static final String DOMAINS_PATH = "/cloudhub/api/applications/domains/";

    public CloudhubApi(String username, String password, String environment)
    {
        super(username, password, environment);
    }

    public Application createApplication(String appName, String region, String muleVersion, int workers, String workerType)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URI).path(APPLICATIONS_PATH);

        CreateApplicationRequest application = new CreateApplicationRequest(appName, region, muleVersion, workers, workerType);

        Response response = target.
                request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).
                post(Entity.entity(application, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() == 201) // Created
        {
            return response.readEntity(Application.class);
        }
        else
        {
            String message = response.readEntity(String.class);
            throw new ApiException(message, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
        }
    }

    public void updateApplication(String appName, String region, String muleVersion, int workers, String workerType)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URI).path(String.format(APPLICATION_UPDATE_PATH, appName));

        UpdateApplicationRequest application = new UpdateApplicationRequest(region, muleVersion, workers, workerType);

        Response response = target.
                request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).
                put(Entity.entity(application, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() != 200 && response.getStatus() != 301) // OK || Not modified
        {
            String message = response.readEntity(String.class);
            throw new ApiException(message, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
        }
    }

    /**
     * Returns the details of an application, or null if it doesn't exist.
     */
    public Application getApplication(String appName)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URI).path(APPLICATIONS_PATH + "/" + appName);

        Response response = target.
                request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).get();

        if (response.getStatus() == 200)
        {
            return response.readEntity(Application.class);
        }
        else if (response.getStatus() == 404) // Not found
        {
            return null;
        }
        else
        {
            String message = response.readEntity(String.class);
            throw new ApiException(message, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
        }
    }

    public void uploadFile(String appName, File file)
    {
        Client client = ClientBuilder.newClient().register(MultiPartFeature.class);
        WebTarget target = client.target(URI).path(String.format(APPLICATIONS_FILES_PATH, appName));
        final FileDataBodyPart applicationPart = new FileDataBodyPart("file", file);
        final MultiPart multipart = new FormDataMultiPart()
                .bodyPart(applicationPart);

        Response response = target.
                request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).
                post(Entity.entity(multipart, multipart.getMediaType()));

        if (response.getStatus() != 200)
        {
            String message = response.readEntity(String.class);
            throw new ApiException(message, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
        }
    }

    public void startApplication(String appName)
    {
        changeApplicationState(appName, "START");
    }

    public void stopApplication(String appName)
    {
        changeApplicationState(appName, "STOP");
    }

    private void changeApplicationState(String appName, String state)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URI).path(APPLICATIONS_PATH + "/" + appName + "/status");
        Entity<String> json = Entity.json("{\"status\": \"" + state + "\"}");

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).
                post(json);

        if (response.getStatus() != 200 && response.getStatus() != 304)
        {
            String message = response.readEntity(String.class);
            throw new ApiException(message, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
        }

    }


    public void deleteApplication(String appName)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URI).path(APPLICATIONS_PATH + "/" + appName);

        Response response = target.
                request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).
                delete();

        if (response.getStatus() != 200 && response.getStatus() != 204)
        {
            String message = response.readEntity(String.class);
            throw new ApiException(message, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
        }

    }


    public boolean isNameAvailable(String appName)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URI).path(DOMAINS_PATH + appName);

        Response response = target.
                request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).get();

        if (response.getStatus() == 200)
        {
            DomainAvailability availability = response.readEntity(DomainAvailability.class);
            return availability.available;
        }
        else
        {
            String message = response.readEntity(String.class);
            throw new ApiException(message, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
        }

    }

    private static class DomainAvailability
    {
        public boolean available;
    }
}

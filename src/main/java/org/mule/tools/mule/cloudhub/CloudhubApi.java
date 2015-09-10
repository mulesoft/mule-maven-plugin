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
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.maven.plugin.logging.Log;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

public class CloudhubApi extends AbstractMuleApi
{

    public static final String URI = "https://anypoint.mulesoft.com";
    public static final String APPLICATIONS_PATH = "/cloudhub/api/applications";
    public static final String APPLICATION_UPDATE_PATH = "/cloudhub/api/v2/applications/%s";
    public static final String APPLICATIONS_FILES_PATH = "/cloudhub/api/v2/applications/%s/files";
    public static final String DOMAINS_PATH = "/cloudhub/api/applications/domains/";

    public CloudhubApi(Log log, String username, String password, String environment)
    {
        super(log, username, password, environment);
    }

    public Application createApplication(String appName, String region, String muleVersion, Integer workers, String workerType)
    {
        CreateApplicationRequest application = new CreateApplicationRequest(appName, region, muleVersion, workers, workerType);
        Response response = post(URI, APPLICATIONS_PATH, application);

        if (response.getStatus() == 201) // Created
        {
            return response.readEntity(Application.class);
        }
        else
        {
            throw new ApiException(response);
        }
    }

    public void updateApplication(String appName, String region, String muleVersion, Integer workers, String workerType)
    {
        UpdateApplicationRequest application = new UpdateApplicationRequest(region, muleVersion, workers, workerType);
        Response response = put(URI, String.format(APPLICATION_UPDATE_PATH, appName), application);

        if (response.getStatus() != 200 && response.getStatus() != 301) // OK || Not modified
        {
            throw new ApiException(response);
        }
    }

    /**
     * Returns the details of an application, or null if it doesn't exist.
     */
    public Application getApplication(String appName)
    {
        Response response = get(URI, APPLICATIONS_PATH + "/" + appName);

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
            throw new ApiException(response);
        }
    }

    public List<Application> getApplications()
    {
        Response response = get(URI, APPLICATIONS_PATH);

        if (response.getStatus() == 200)
        {
            return response.readEntity(new GenericType<List<Application>>() { } );
        }
        else
        {
            throw new ApiException(response);
        }
    }

    public void uploadFile(String appName, File file)
    {
        FileDataBodyPart applicationPart = new FileDataBodyPart("file", file);
        MultiPart multipart = new FormDataMultiPart().bodyPart(applicationPart);

        Response response = post(URI, String.format(APPLICATIONS_FILES_PATH, appName), Entity.entity(multipart, multipart.getMediaType()));

        if (response.getStatus() != 200)
        {
            throw new ApiException(response);
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
        Entity<String> json = Entity.json("{\"status\": \"" + state + "\"}");
        Response response = post(URI, APPLICATIONS_PATH + "/" + appName + "/status", json);

        if (response.getStatus() != 200 && response.getStatus() != 304)
        {
            throw new ApiException(response);
        }

    }


    public void deleteApplication(String appName)
    {
        Response response = delete(URI, APPLICATIONS_PATH + "/" + appName);

        if (response.getStatus() != 200 && response.getStatus() != 204)
        {
            throw new ApiException(response);
        }

    }


    public boolean isNameAvailable(String appName)
    {
        Response response = get(URI, DOMAINS_PATH + appName);

        if (response.getStatus() == 200)
        {
            DomainAvailability availability = response.readEntity(DomainAvailability.class);
            return availability.available;
        }
        else
        {
            throw new ApiException(response);
        }

    }

    private static class DomainAvailability
    {
        public boolean available;
    }
}

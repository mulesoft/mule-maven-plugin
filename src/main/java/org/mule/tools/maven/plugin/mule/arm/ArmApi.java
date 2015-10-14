/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule.arm;

import org.mule.tools.maven.plugin.mule.AbstractMuleApi;
import org.mule.tools.maven.plugin.mule.TargetType;

import java.io.File;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.maven.plugin.logging.Log;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

public class ArmApi extends AbstractMuleApi
{

    private static final String APPLICATIONS = "/hybrid/api/v1/applications";
    private static final String SERVERS = "/hybrid/api/v1/servers";
    private static final String SERVER_GROUPS = "/hybrid/api/v1/serverGroups";
    private static final String CLUSTERS = "/hybrid/api/v1/clusters";

    public ArmApi(Log log, String uri, String username, String password, String environment, String businessGroup)
    {
        super(uri, log, username, password, environment, businessGroup);
    }

    public Boolean isStarted(int applicationId)
    {
        Application application = getApplicationStatus(applicationId);
        return "STARTED".equals(application.data.lastReportedStatus);
    }

    public Application getApplicationStatus(int applicationId)
    {
        return get(uri, APPLICATIONS + "/" + applicationId, Application.class);
    }

    public String undeployApplication(int applicationId)
    {
        Response response = delete(uri, APPLICATIONS + "/" + applicationId);
        return response.readEntity(String.class);
    }

    public Application deployApplication(File app, String appName, TargetType targetType, String target)
    {
        String id = getId(targetType, target);

        FileDataBodyPart applicationPart = new FileDataBodyPart("file", app);
        MultiPart multipart = new FormDataMultiPart()
                .field("artifactName", appName)
                .field("targetId", id)
                .bodyPart(applicationPart);

        Response response = post(uri, APPLICATIONS, Entity.entity(multipart, multipart.getMediaType()));
        validateStatusSuccess(response);
        return response.readEntity(Application.class);
    }

    private String getId(TargetType targetType, String target)
    {
        String id = null;
        switch (targetType)
        {
            case server:
                id = findServerByName(target).id;
                break;
            case serverGroup:
                id = findServerGroupByName(target).id;
                break;
            case cluster:
                id = findClusterByName(target).id;
                break;
        }
        return id;
    }

    public Applications getApplications()
    {
        return get(uri, APPLICATIONS, Applications.class);
    }

    public Target findServerByName(String name)
    {
        return findTargetByName(name, SERVERS);
    }

    public Target findServerGroupByName(String name)
    {
        return findTargetByName(name, SERVER_GROUPS);
    }

    public Target findClusterByName(String name)
    {
        return findTargetByName(name, CLUSTERS);
    }

    private Target findTargetByName(String name, String path)
    {
        Targets response = get(uri, path, Targets.class);
        if (response.data == null) // Workaround because an empty array in the response is mapped as null
        {
            throw new RuntimeException("Couldn't find target named [" + name + "]");
        }
        for (int i = 0; i < response.data.length; i++)
        {
            if (name.equals(response.data[i].name))
            {
                return response.data[i];
            }
        }
        throw new RuntimeException("Couldn't find target named [" + name + "]");
    }

    public Integer findApplication(String name)
    {
        Data[] applications = getApplications().data;
        if (applications == null)
        {
            return null;
        }
        for (int i = 0 ; i < applications.length ; i ++ )
        {
            if (name.equals(applications[i].artifact.name))
            {
                return applications[i].id;
            }
        }
        return null;
    }
}

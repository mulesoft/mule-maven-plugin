/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule.arm;

import org.mule.tools.mule.AbstractMuleApi;
import org.mule.tools.mule.TargetType;

import java.io.File;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

public class ArmApi extends AbstractMuleApi
{

    private static final String APPLICATIONS = "/hybrid/api/v1/applications";
    private static final String SERVERS = "/hybrid/api/v1/servers";
    private static final String SERVER_GROUPS = "/hybrid/api/v1/serverGroups";
    private static final String CLUSTERS = "/hybrid/api/v1/clusters";

    private String uri;

    public ArmApi(String uri, String username, String password, String environment)
    {
        super(username, password, environment);
        this.uri = uri;
    }

    public Boolean isStarted(int applicationId)
    {
        Application application = getApplicationStatus(applicationId);
        return "STARTED".equals(application.data.lastReportedStatus);
    }

    public Application getApplicationStatus(int applicationId)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri).path(APPLICATIONS + "/" + applicationId);
        return target.
                request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).
                get(Application.class);
    }

    public String undeployApplication(int applicationId)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri).path(APPLICATIONS + "/" + applicationId);
        return target.
                request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).
                delete(String.class);
    }

    public Application deployApplication(File app, String appName, TargetType targetType, String target)
    {
        String id = getId(targetType, target);
        Client client = ClientBuilder.newClient().register(MultiPartFeature.class);
        WebTarget webTarget = client.target(uri).path(APPLICATIONS);
        final FileDataBodyPart applicationPart = new FileDataBodyPart("file", app);
        final MultiPart multipart = new FormDataMultiPart()
                .field("artifactName", appName)
                .field("targetId", id)
                .bodyPart(applicationPart);

        return webTarget.
                request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).
                post(Entity.entity(multipart, multipart.getMediaType()), Application.class);
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
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri).path(APPLICATIONS);
        return target.request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).get(Applications.class);
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
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri).path(path);
        Targets response = target.request(MediaType.APPLICATION_JSON_TYPE).
                headers(authorizationHeader()).get(Targets.class);
        for (int i = 0; i < response.data.length; i++)
        {
            if (name.equals(response.data[i].name))
            {
                return response.data[i];
            }
        }
        throw new RuntimeException("Couldn't find target named [" + name + "]");
    }

}

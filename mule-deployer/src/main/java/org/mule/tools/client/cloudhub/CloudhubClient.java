/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.cloudhub;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.mule.tools.client.AbstractMuleClient;
import org.mule.tools.client.exception.ClientException;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;

public class CloudhubClient extends AbstractMuleClient {

  public static final String APPLICATIONS_PATH = "/cloudhub/api/applications";
  public static final String APPLICATION_UPDATE_PATH = "/cloudhub/api/v2/applications/%s";
  public static final String APPLICATIONS_FILES_PATH = "/cloudhub/api/v2/applications/%s/files";
  public static final String DOMAINS_PATH = "/cloudhub/api/applications/domains/";
  public static final String CREATE_REQUEST_TEMPLATE = "{" +
      "  \"domain\": \"%s\"," +
      "  \"region\": \"%s\"," +
      "  \"muleVersion\": \"%s\"," +
      "  \"workers\": %d," +
      "  \"workerType\": \"%s\"";
  public static final String UPDATE_REQUEST_TEMPLATE = "{" +
      "  \"region\":\"%s\"," +
      "  \"muleVersion\": {\"version\": \"%s\"}," +
      "  \"workers\": {" +
      "    \"amount\": %d," +
      "    \"type\": {" +
      "        \"name\": \"%s\"" +
      "    }" +
      "  }";
  private static final int OK = 200;
  private static final int CREATED = 201;
  private static final int NO_CONTENT = 204;
  private static final int MOVED_PERMANENTLY = 301;
  private static final int NOT_MODIFIED = 304;
  private static final int NOT_FOUND = 404;

  public CloudhubClient(CloudHubDeployment cloudhubDeployment, DeployerLog log) {
    super(cloudhubDeployment, log);
  }

  public Application createApplication(String appName, String region, String muleVersion, Integer workers, String workerType,
                                       Map<String, String> properties) {
    Entity<String> json = createApplicationRequest(appName, region, muleVersion, workers, workerType, properties);
    Response response = post(uri, APPLICATIONS_PATH, json);
    if (response.getStatus() == CREATED) {
      return response.readEntity(Application.class);
    } else {
      throw new ClientException(response);
    }
  }

  private Entity<String> createApplicationRequest(String appName, String region, String muleVersion, Integer workers,
                                                  String workerType, Map<String, String> properties) {
    String json = String.format(CREATE_REQUEST_TEMPLATE, appName, region, muleVersion, workers, workerType);
    json = addProperties(properties, json);
    json = json + "}";
    return Entity.json(json);
  }

  private Entity<String> updateApplicationRequest(String region, String muleVersion, Integer workers, String workerType,
                                                  Map<String, String> properties) {
    String json = String.format(UPDATE_REQUEST_TEMPLATE, region, muleVersion, workers, workerType);
    json = addProperties(properties, json);
    json = json + "}";
    return Entity.json(json);
  }

  private String addProperties(Map<String, String> properties, String json) {
    if (properties == null) {
      return json;
    }

    json = json + ",";
    json = json + "  \"properties\": {";
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      json = json + "    \"" + entry.getKey() + "\":\"" + entry.getValue() + "\",";
    }
    if (json.charAt(json.length() - 1) == ',') {
      json = json.substring(0, json.length() - 1);
    }
    json = json + "  }\n";
    return json;
  }

  public void updateApplication(String appName, String region, String muleVersion, Integer workers, String workerType,
                                Map<String, String> properties) {
    Entity<String> json = updateApplicationRequest(region, muleVersion, workers, workerType, properties);
    Response response = put(uri, String.format(APPLICATION_UPDATE_PATH, appName), json);
    if (response.getStatus() != OK && response.getStatus() != MOVED_PERMANENTLY) {
      throw new ClientException(response);
    }
  }

  /**
   * Looks up an application by its name.
   * 
   * @param appName The application name to look up.
   * @return The details of an application, or null if it doesn't exist.
   */
  public Application getApplication(String appName) {
    Response response = get(uri, APPLICATIONS_PATH + "/" + appName);

    if (response.getStatus() == OK) {
      return response.readEntity(Application.class);
    } else if (response.getStatus() == NOT_FOUND) {
      return null;
    } else {
      throw new ClientException(response);
    }
  }

  public List<Application> getApplications() {
    Response response = get(uri, APPLICATIONS_PATH);

    if (response.getStatus() == OK) {
      return response.readEntity(new GenericType<List<Application>>() {});
    } else {
      throw new ClientException(response);
    }
  }

  public void uploadFile(String appName, File file) {
    FileDataBodyPart applicationPart = new FileDataBodyPart("file", file);
    MultiPart multipart = new FormDataMultiPart().bodyPart(applicationPart);

    Response response =
        post(uri, String.format(APPLICATIONS_FILES_PATH, appName), Entity.entity(multipart, multipart.getMediaType()));

    if (response.getStatus() != OK) {
      throw new ClientException(response);
    }
  }

  public void startApplication(String appName) {
    changeApplicationState(appName, "START");
  }

  public void stopApplication(String appName) {
    changeApplicationState(appName, "STOP");
  }

  private void changeApplicationState(String appName, String state) {
    Entity<String> json = Entity.json("{\"status\": \"" + state + "\"}");
    Response response = post(uri, APPLICATIONS_PATH + "/" + appName + "/status", json);

    if (response.getStatus() != OK && response.getStatus() != NOT_MODIFIED) {
      throw new ClientException(response);
    }

  }


  public void deleteApplication(String appName) {
    Response response = delete(uri, APPLICATIONS_PATH + "/" + appName);

    if (response.getStatus() != OK && response.getStatus() != NO_CONTENT) {
      throw new ClientException(response);
    }

  }


  public boolean isNameAvailable(String appName) {
    Response response = get(uri, DOMAINS_PATH + appName);

    if (response.getStatus() == OK) {
      DomainAvailability availability = response.readEntity(DomainAvailability.class);
      return availability.available;
    } else {
      throw new ClientException(response);
    }

  }

  private static class DomainAvailability {

    public boolean available;
  }
}

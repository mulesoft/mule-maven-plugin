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
import java.util.*;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.mule.tools.client.AbstractMuleClient;
import org.mule.tools.client.exception.ClientException;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;

public class CloudHubClient extends AbstractMuleClient {

  public static final String STARTED_STATUS = "STARTED";
  public static final String UNDEPLOYED_STATUS = "UNDEPLOYED";

  private static final String APPLICATIONS_PATH = "/cloudhub/api/applications";
  private static final String DOMAINS_PATH = "/cloudhub/api/applications/domains/";
  private static final String APPLICATION_UPDATE_PATH = "/cloudhub/api/v2/applications/%s";
  private static final String APPLICATIONS_FILES_PATH = "/cloudhub/api/v2/applications/%s/files";
  private static final String SUPPORTED_VERSIONS_PATH = "cloudhub/api/mule-versions";
  private static final String CREATE_REQUEST_TEMPLATE = "{" +
      "  \"domain\": \"%s\"," +
      "  \"region\": \"%s\"," +
      "  \"muleVersion\": \"%s\"," +
      "  \"workers\": %d," +
      "  \"workerType\": \"%s\"";
  private static final String UPDATE_REQUEST_TEMPLATE = "{" +
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

  public CloudHubClient(CloudHubDeployment cloudhubDeployment, DeployerLog log) {
    super(cloudhubDeployment, log);
  }

  public Application createApplication(ApplicationMetadata metadata) {
    Entity<String> json = createApplicationRequest(metadata);
    Response response = post(baseUri, APPLICATIONS_PATH, json);
    if (response.getStatus() == CREATED) {
      return response.readEntity(Application.class);
    } else {
      throw new ClientException(response);
    }
  }

  private Entity<String> createApplicationRequest(ApplicationMetadata metadata) {
    String json = String.format(CREATE_REQUEST_TEMPLATE, metadata.getName(), metadata.getRegion(),
                                metadata.getMuleVersion().get(), metadata.getWorkers(),
                                metadata.getWorkerType());
    json = addProperties(metadata.getProperties(), json);
    json = json + "}";
    return Entity.json(json);
  }

  private Entity<String> updateApplicationRequest(ApplicationMetadata metadata) {
    String json =
        String.format(UPDATE_REQUEST_TEMPLATE, metadata.getRegion(), metadata.getMuleVersion().get(), metadata.getWorkers(),
                      metadata.getWorkerType());
    json = addProperties(metadata.getProperties(), json);
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

  public void updateApplication(ApplicationMetadata metadata) {
    Entity<String> json = updateApplicationRequest(metadata);
    Response response = put(baseUri, String.format(APPLICATION_UPDATE_PATH, metadata.getName()), json);
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
    Response response = get(baseUri, APPLICATIONS_PATH + "/" + appName);

    if (response.getStatus() == OK) {
      return response.readEntity(Application.class);
    } else if (response.getStatus() == NOT_FOUND) {
      return null;
    } else {
      throw new ClientException(response);
    }
  }

  public List<Application> getApplications() {
    Response response = get(baseUri, APPLICATIONS_PATH);

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
        post(baseUri, String.format(APPLICATIONS_FILES_PATH, appName), Entity.entity(multipart, multipart.getMediaType()));

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
    Response response = post(baseUri, APPLICATIONS_PATH + "/" + appName + "/status", json);

    if (response.getStatus() != OK && response.getStatus() != NOT_MODIFIED) {
      throw new ClientException(response);
    }

  }

  public void deleteApplication(String appName) {
    Response response = delete(baseUri, APPLICATIONS_PATH + "/" + appName);

    if (response.getStatus() != OK && response.getStatus() != NO_CONTENT) {
      throw new ClientException(response);
    }

  }


  public boolean isNameAvailable(String appName) {
    Response response = get(baseUri, DOMAINS_PATH + appName);

    if (response.getStatus() == OK) {
      DomainAvailability availability = response.readEntity(DomainAvailability.class);
      return availability.available;
    } else {
      throw new ClientException(response);
    }

  }

  public Set<String> getSupportedMuleVersions() {
    Set<String> supportedMuleVersions = new HashSet<>();

    String jsonResponse = get(baseUri, SUPPORTED_VERSIONS_PATH).readEntity(String.class);

    JsonObject response = new JsonParser().parse(jsonResponse).getAsJsonObject();
    JsonArray dataElements = response.get("data").getAsJsonArray();
    for (JsonElement dataElement : dataElements) {
      JsonObject data = dataElement.getAsJsonObject();
      String muleVersion = data.get("version").getAsString();
      supportedMuleVersions.add(muleVersion);
    }

    return supportedMuleVersions;
  }

  private static class DomainAvailability {

    public boolean available;
  }
}

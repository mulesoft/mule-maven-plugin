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

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import org.mule.tools.client.AbstractMuleClient;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.model.DomainAvailability;
import org.mule.tools.client.cloudhub.model.PaginatedResponse;
import org.mule.tools.client.cloudhub.model.SupportedVersion;
import org.mule.tools.client.exception.ClientException;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CloudHubClient extends AbstractMuleClient {

  private static final String API_VERSION = "v2";
  private static final String BASE_PATH = "/cloudhub/api";

  // TODO using version less
  private static final String SUPPORTED_VERSIONS_PATH = BASE_PATH + "/mule-versions";
  private static final String APPLICATION_STATUS = BASE_PATH + "/applications/%s/status";
  private static final String APPLICATIONS_DOMAINS_PATH = BASE_PATH + "/applications" + "/domains/%s";

  private static final String BASE_API_PATH = BASE_PATH + "/" + API_VERSION;

  private static final String APPLICATIONS_PATH = BASE_API_PATH + "/applications";
  private static final String A_APPLICATION_PATH = APPLICATIONS_PATH + "/%s";


  private static final int OK = 200;
  private static final int CREATED = 201;
  private static final int NO_CONTENT = 204;
  private static final int MOVED_PERMANENTLY = 301;
  private static final int NOT_MODIFIED = 304;
  private static final int NOT_FOUND = 404;

  public CloudHubClient(CloudHubDeployment cloudhubDeployment, DeployerLog log) {
    super(cloudhubDeployment, log);
  }

  public List<Application> getApplications() {
    Response response = get(baseUri, APPLICATIONS_PATH);

    if (response.getStatus() == OK) {
      return response.readEntity(new GenericType<List<Application>>() {});
    } else {
      throw new ClientException(response);
    }
  }

  public Application getApplication(String domain) {
    checkArgument(isNotBlank(domain), "The domain must not be null nor empty.");

    Response response = get(baseUri, format(A_APPLICATION_PATH, domain));

    if (response.getStatus() == OK) {
      return response.readEntity(Application.class);
    }

    if (response.getStatus() == NOT_FOUND) {
      // TODO this should throw an exception
      return null;
    } else {
      throw new ClientException(response);
    }
  }

  public Application createApplication(Application application, File file) {
    checkArgument(file != null, "The file must not be null.");
    checkArgument(application != null, "The application must not be null.");

    // TODO refactor this message for app request
    FileDataBodyPart filePart = new FileDataBodyPart("file", file);
    String applicationJson = new Gson().toJson(application);
    FormDataBodyPart appInfoJsonPart = new FormDataBodyPart("appInfoJson", applicationJson);

    MultiPart multipart = new FormDataMultiPart().bodyPart(filePart).bodyPart(appInfoJsonPart);
    Entity<MultiPart> entity = Entity.entity(multipart, multipart.getMediaType());

    // TODO avoid logging file content
    Response response = post(baseUri, APPLICATIONS_PATH, entity);

    if (response.getStatus() == OK) {
      return response.readEntity(Application.class);
    } else {
      throw new ClientException(response);
    }
  }

  // TODO this should receive an applicaiton object
  public Application updateApplication(Application application, File file) {
    checkArgument(file != null, "The file must not be null.");
    checkArgument(application != null, "The application must not be null.");
    checkArgument(isNotBlank(application.getDomain()), "The application domain must not be null nor empty.");

    // TODO refactor this message for app request
    FileDataBodyPart filePart = new FileDataBodyPart("file", file);
    String applicationJson = new Gson().toJson(application);
    FormDataBodyPart appInfoJsonPart = new FormDataBodyPart("appInfoJson", applicationJson);

    MultiPart multipart = new FormDataMultiPart().bodyPart(filePart).bodyPart(appInfoJsonPart);
    Entity<MultiPart> entity = Entity.entity(multipart, multipart.getMediaType());

    // TODO avoid logging file content
    Response response = put(baseUri, format(A_APPLICATION_PATH, application.getDomain()), entity);

    if (response.getStatus() == OK) {
      return response.readEntity(Application.class);
    } else {
      throw new ClientException(response);
    }
  }

  public void deleteApplication(String domain) {
    checkArgument(isNotBlank(domain), "The domain must not be null nor empty.");

    Response response = delete(baseUri, format(A_APPLICATION_PATH, domain));

    if (response.getStatus() != OK && response.getStatus() != NO_CONTENT) {
      throw new ClientException(response);
    }
  }

  public void startApplication(String domain) {
    Application application = new Application();
    application.setStatus("START");

    Response response = post(baseUri, format(APPLICATION_STATUS, domain), new Gson().toJson(application));

    if (response.getStatus() != OK && response.getStatus() != NOT_MODIFIED) {
      throw new ClientException(response);
    }
  }

  public void stopApplication(String domain) {
    Application application = new Application();
    application.setStatus("STOP");

    Response response = post(baseUri, format(APPLICATION_STATUS, domain), new Gson().toJson(application));

    if (response.getStatus() != OK && response.getStatus() != NOT_MODIFIED) {
      throw new ClientException(response);
    }
  }

  public boolean isDomainAvailable(String appName) {
    Response response = get(baseUri, format(APPLICATIONS_DOMAINS_PATH, appName));

    if (response.getStatus() == OK) {
      DomainAvailability availability = response.readEntity(DomainAvailability.class);
      return availability.isAvailable();
    } else {
      throw new ClientException(response);
    }
  }

  public List<SupportedVersion> getSupportedMuleVersions() {
    Response response = get(baseUri, SUPPORTED_VERSIONS_PATH);

    if (response.getStatus() == OK) {
      String jsonResponse = response.readEntity(String.class);
      Type type = new TypeToken<PaginatedResponse<SupportedVersion>>() {}.getType();
      PaginatedResponse<SupportedVersion> paginatedResponse = new Gson().fromJson(jsonResponse, type);

      return paginatedResponse.getData();
    }
    throw new ClientException(response);
  }

}

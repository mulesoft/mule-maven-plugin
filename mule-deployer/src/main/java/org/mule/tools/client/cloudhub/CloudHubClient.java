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
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.mule.tools.client.AbstractMuleClient;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.model.Deployment;
import org.mule.tools.client.cloudhub.model.DeploymentLogRequest;
import org.mule.tools.client.cloudhub.model.DomainAvailability;
import org.mule.tools.client.cloudhub.model.Environment;
import org.mule.tools.client.cloudhub.model.LogRecord;
import org.mule.tools.client.cloudhub.model.PaginatedResponse;
import org.mule.tools.client.cloudhub.model.SupportedVersion;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

/**
 * Client to hit the CloudHub API
 */
public class CloudHubClient extends AbstractMuleClient {

  private static final String API_VERSION = "v2";
  public static final String BASE_API_PATH = "/cloudhub/api";

  // TODO using version less
  public static final String SUPPORTED_VERSIONS_PATH = BASE_API_PATH + "/mule-versions";
  public static final String APPLICATION_STATUS = BASE_API_PATH + "/applications/%s/status";
  public static final String APPLICATIONS_DOMAINS_PATH = BASE_API_PATH + "/applications" + "/domains/%s";

  public static final String BASE_API_VERSION_PATH = BASE_API_PATH + "/" + API_VERSION;

  public static final String APPLICATIONS_PATH = BASE_API_VERSION_PATH + "/applications";
  public static final String A_APPLICATION_PATH = APPLICATIONS_PATH + "/%s";
  public static final String A_APPLICATION_LOGS = A_APPLICATION_PATH + "/logs";
  public static final String A_INSTANCE_LOGS = A_APPLICATION_PATH + "/instances/%s/log-file";
  public static final String DEPLOYMENTS_PATH = APPLICATIONS_PATH + "/%s/deployments";
  public static final String APPLICATION_ENVIRONMENT = BASE_API_PATH + "/buildinfo/environment";

  public CloudHubClient(CloudHubDeployment cloudhubDeployment, DeployerLog log) {
    super(cloudhubDeployment, log);
  }

  /**
   * Look up all the applications
   *
   * @return a list with all the {@link Application}
   */
  public List<Application> getApplications() {
    Response response = get(baseUri, APPLICATIONS_PATH);

    checkResponseStatus(response, OK);

    return response.readEntity(new GenericType<List<Application>>() {});
  }

  /**
   * Look up a {@link Application} based on its domain name
   *
   * @param domain the domain name of the application
   * @return null if none found, otherwise the {@link Application}
   */
  public Application getApplications(String domain) {
    checkArgument(isNotBlank(domain), "The domain must not be null nor empty.");

    Response response = get(baseUri, format(A_APPLICATION_PATH, domain));

    checkResponseStatus(response, OK, NOT_FOUND);

    if (response.getStatus() == OK.getStatusCode()) {
      return readJsonEntity(response, Application.class);
    }

    return null;
  }

  /**
   * Creates an {@link Application}
   *
   * @param application the {@link Application} entity
   * @param file the file of the {@link Application}
   * @return the {@link Application} just created
   */
  public Application createApplication(Application application, File file) {
    checkArgument(file != null, "The file must not be null.");
    checkArgument(application != null, "The application must not be null.");

    Entity<MultiPart> entity = getMultiPartEntity(application, file);

    Response response = post(baseUri, APPLICATIONS_PATH, entity);

    checkResponseStatus(response, OK);

    return response.readEntity(Application.class);
  }

  /**
   * Update an already existing {@link Application}
   *
   * @param application the {@link Application} entity
   * @param file the file of the {@link Application}
   * @return the {@link Application} just updated
   */
  public Application updateApplication(Application application, File file) {
    checkArgument(file != null, "The file must not be null.");
    checkArgument(application != null, "The application must not be null.");
    checkArgument(isNotBlank(application.getDomain()), "The application domain must not be null nor empty.");

    Entity<MultiPart> entity = getMultiPartEntity(application, file);

    Response response = put(baseUri, format(A_APPLICATION_PATH, application.getDomain()), entity);

    checkResponseStatus(response, OK);

    return response.readEntity(Application.class);
  }

  /**
   * Deletes an {@link Application} based on its domain name
   *
   * @param domain the domain name of the application
   */
  public void deleteApplications(String domain) {
    checkArgument(isNotBlank(domain), "The domain must not be null nor empty.");

    Response response = delete(baseUri, format(A_APPLICATION_PATH, domain));

    checkResponseStatus(response, OK, NO_CONTENT);
  }

  /**
   * Starts an {@link Application} based on its domain name
   *
   * @param domain the domain name of the application
   */
  public void startApplications(String domain) {
    checkArgument(isNotBlank(domain), "The domain must not be null nor empty.");

    Application application = new Application();
    application.setStatus("START");

    Response response = post(baseUri, format(APPLICATION_STATUS, domain), new Gson().toJson(application));

    checkResponseStatus(response, OK, NOT_MODIFIED);
  }

  /**
   * Stops an {@link Application} based on its domain name
   *
   * @param domain the domain name of the application
   */
  public void stopApplications(String domain) {
    checkArgument(isNotBlank(domain), "The domain must not be null nor empty.");

    Application application = new Application();
    application.setStatus("STOP");

    Response response = post(baseUri, format(APPLICATION_STATUS, domain), new Gson().toJson(application));

    checkResponseStatus(response, OK, NOT_MODIFIED);
  }

  /**
   * It checks the availability of a given doamain name
   *
   * @param domain the domain name
   * @return false if the domain is not available, true otherwise
   */
  public boolean isDomainAvailable(String domain) {
    checkArgument(isNotBlank(domain), "The domain must not be null nor empty.");

    Response response = get(baseUri, format(APPLICATIONS_DOMAINS_PATH, domain));

    checkResponseStatus(response, OK);

    DomainAvailability availability = response.readEntity(DomainAvailability.class);
    return availability.isAvailable();
  }

  /**
   * Retrieve a list of {@link SupportedVersion}
   *
   * @return a list of the {@link SupportedVersion}
   */
  public List<SupportedVersion> getSupportedMuleVersions() {
    Response response = get(baseUri, SUPPORTED_VERSIONS_PATH);

    checkResponseStatus(response, OK);

    Type type = new TypeToken<PaginatedResponse<SupportedVersion>>() {}.getType();
    PaginatedResponse<SupportedVersion> paginatedResponse = readJsonEntity(response, type);

    return paginatedResponse.getData();
  }

  /**
   * Retrieve the list of {@link Deployment} for an {@link Application}
   * 
   * @param application the application
   * @return a list of {@link Deployment}
   */
  public List<Deployment> getDeployments(Application application) {
    checkArgument(application != null, "The application must not be null.");

    Response response = get(baseUri, format(DEPLOYMENTS_PATH, application.getDomain()));

    checkResponseStatus(response, OK);

    Type type = new TypeToken<PaginatedResponse<Deployment>>() {}.getType();
    PaginatedResponse<Deployment> paginatedResponse = readJsonEntity(response, type);

    return paginatedResponse.getData();
  }

  /**
   * Retrieves the logs records from an {@link Application} given a {@link DeploymentLogRequest}
   *
   * @param application the application
   * @param deploymentLogRequest the specific request filter for the logs
   * @return a list of {@link LogRecord}
   */
  public List<LogRecord> getLogs(Application application, DeploymentLogRequest deploymentLogRequest) {
    checkArgument(deploymentLogRequest != null, "The log request must not be null.");
    checkArgument(application != null, "The application must not be null.");

    Response response = post(baseUri, format(A_APPLICATION_LOGS, application.getDomain()), deploymentLogRequest);

    checkResponseStatus(response, OK);

    return response.readEntity(new GenericType<List<LogRecord>>() {});
  }

  /**
   * Retrieves the entire logs records from an {@link Application} given the instanceId
   *
   * @param application the application
   * @param instanceId the id of the instance to get the logs
   * @return a {@link InputStream}
   */
  public InputStream getEntireLogs(Application application, String instanceId) {
    checkArgument(instanceId != null, "The instanceId must not be null.");
    checkArgument(application != null, "The application must not be null.");

    Response response = get(baseUri, format(A_INSTANCE_LOGS, application.getDomain(), instanceId));

    checkResponseStatus(response, OK);

    return response.readEntity(InputStream.class);
  }

  private Entity<MultiPart> getMultiPartEntity(Application application, File file) {
    FileDataBodyPart filePart = new FileDataBodyPart("file", file);

    FormDataBodyPart appInfoJsonPart = new FormDataBodyPart("appInfoJson", new Gson().toJson(application));

    MultiPart multipart = new FormDataMultiPart().bodyPart(filePart).bodyPart(appInfoJsonPart);
    return Entity.entity(multipart, multipart.getMediaType());
  }

  public Environment getEnvironment() {
    Response response = get(baseUri, APPLICATION_ENVIRONMENT);

    checkResponseStatus(response, OK);
    return response.readEntity(Environment.class);
  }
}

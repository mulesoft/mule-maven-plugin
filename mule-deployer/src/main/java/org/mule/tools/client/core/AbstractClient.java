/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.core;

import static com.google.common.net.HttpHeaders.USER_AGENT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.glassfish.jersey.client.ClientProperties.CHUNKED_ENCODING_SIZE;
import static org.glassfish.jersey.client.ClientProperties.DEFAULT_CHUNK_SIZE;
import static org.glassfish.jersey.client.ClientProperties.REQUEST_ENTITY_PROCESSING;
import static org.glassfish.jersey.client.HttpUrlConnectorProvider.SET_METHOD_WORKAROUND;
import static org.mule.tools.client.authentication.AuthenticationServiceClient.LOGIN;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import org.mule.tools.client.core.exception.ClientException;
import org.mule.tools.client.core.logging.ClientLoggingFilter;
import org.mule.tools.utils.DeployerLog;

import com.google.gson.Gson;

@SuppressWarnings("rawtypes")
public abstract class AbstractClient {

  private String userAgent = "mule-deployer%s";

  protected DeployerLog log;

  private boolean isClientInitialized = false;

  public AbstractClient() {}

  public AbstractClient(DeployerLog log) {
    this.log = log;
  }

  protected Response post(String uri, String path, Entity entity) {
    initialize();
    return builder(uri, path).post(entity);
  }

  protected Response post(String uri, String path, Object entity) {
    initialize();
    return post(uri, path, Entity.entity(entity, APPLICATION_JSON_TYPE));
  }

  protected Response post(String uri, Supplier<String> pathSupplier, Object entity) {
    initialize();
    return post(uri, pathSupplier.get(), Entity.entity(entity, APPLICATION_JSON_TYPE));
  }

  protected Response put(String uri, String path, Entity entity) {
    initialize();
    return builder(uri, path).put(entity);
  }

  protected Response put(String uri, String path, Object entity) {
    initialize();
    return put(uri, path, Entity.entity(entity, APPLICATION_JSON_TYPE));
  }

  protected Response delete(String uri, String path) {
    initialize();
    return builder(uri, path).delete();
  }

  protected Response get(String uri, String path) {
    initialize();
    return builder(uri, path).get();
  }

  protected Response get(String uri, Supplier<String> pathSupplier) {
    initialize();
    return builder(uri, pathSupplier.get()).get();
  }

  protected Response delete(String uri, Supplier<String> pathSupplier) {
    initialize();
    return builder(uri, pathSupplier.get()).delete();
  }

  protected <T> T get(String uri, String path, Class<T> clazz) {
    initialize();
    return get(uri, path).readEntity(clazz);
  }

  protected Response patch(String uri, Supplier<String> path, Object entity) {
    initialize();
    return patch(uri, path.get(), Entity.entity(entity, APPLICATION_JSON_TYPE));
  }

  protected Response patch(String uri, String path, Entity entity) {
    initialize();
    Invocation.Builder builder = builder(uri, path);
    builder.property(SET_METHOD_WORKAROUND, true);
    return builder.method("PATCH", entity);
  }


  public synchronized void initialize() {
    if (!isClientInitialized) {
      isClientInitialized = true;
      init();
    }
  }

  protected abstract void init();

  private Invocation.Builder builder(String uri, String path) {
    WebTarget target = getTarget(uri, path);
    Invocation.Builder builder = target.request(APPLICATION_JSON_TYPE).header(USER_AGENT, getUserAgent());
    setBuilderProperties(builder);
    configureRequest(builder);
    return builder;
  }

  protected WebTarget getTarget(String uri, String path) {
    ClientConfig configuration = new ClientConfig();
    configuration.connectorProvider(new HttpUrlConnectorProvider());
    ClientBuilder builder = ClientBuilder.newBuilder().withConfig(configuration);


    configureSecurityContext(builder);
    Client client = builder.build().register(MultiPartFeature.class);
    if (log != null && log.isDebugEnabled() && !isLoginRequest(path)) {
      client.register(new ClientLoggingFilter(log));
    }

    return client.target(uri).path(path);
  }

  // TODO find a more generic way of doing this
  private boolean isLoginRequest(String path) {
    return LOGIN.equals(path);
  }

  protected String getUserAgent() {
    Package classPackage = AbstractClient.class.getPackage();
    String implementationVersion = classPackage != null ? classPackage.getImplementationVersion() : EMPTY;

    String version = isNotBlank(implementationVersion) ? "/" + implementationVersion : EMPTY;
    return String.format(userAgent, version);
  }

  /**
   * Template method to allow subclasses to configure the request (adding headers for example).
   *
   * @param builder The invocation builder for the request.
   */
  protected void configureRequest(Invocation.Builder builder) {

  }

  protected void configureSecurityContext(ClientBuilder builder) {
    // Implemented in concrete classes
  }

  protected void checkResponseStatus(Response response) {
    if (familyOf(response.getStatus()) != SUCCESSFUL) {
      throw new ClientException(response);
    }
  }

  protected void checkResponseStatus(Response response, Status... expectedStatus) {
    // TODO yes it can be done with a lambda but after cp to 2.x
    List<Integer> success = new ArrayList<>();
    for (Status s : expectedStatus) {
      success.add(s.getStatusCode());
    }

    Integer statusCode = response.getStatus();
    if (!success.contains(statusCode)) {
      throw new ClientException(response);
    }
  }

  // TODO find a way to dec
  protected <T> T readJsonEntity(Response response, Type type) {
    String jsonResponse = response.readEntity(String.class);
    return new Gson().fromJson(jsonResponse, type);
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  /**
   * Method to configure properties needed to stream Entities. Default chunk size is 4096.
   *
   * @param builder The invocation builder for the request.
   */
  private void setBuilderProperties(Invocation.Builder builder) {
    builder.property(REQUEST_ENTITY_PROCESSING, "CHUNKED");
    builder.property(CHUNKED_ENCODING_SIZE, DEFAULT_CHUNK_SIZE);
  }

}

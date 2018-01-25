/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
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
import static org.glassfish.jersey.client.HttpUrlConnectorProvider.SET_METHOD_WORKAROUND;
import static org.mule.tools.client.authentication.AuthenticationServiceClient.LOGIN;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

import org.mule.tools.client.core.exception.ClientException;
import org.mule.tools.client.core.logging.ClientLoggingFilter;
import org.mule.tools.utils.DeployerLog;

import com.google.gson.Gson;

public abstract class AbstractClient {

  private static final String USER_AGENT_MULE_DEPLOYER = "mule-deployer%s";

  protected DeployerLog log;

  public AbstractClient() {}

  public AbstractClient(DeployerLog log) {
    this.log = log;
  }

  protected Response post(String uri, String path, Entity entity) {
    return builder(uri, path).post(entity);
  }

  protected Response post(String uri, String path, Object entity) {
    return post(uri, path, Entity.entity(entity, APPLICATION_JSON_TYPE));
  }

  protected Response put(String uri, String path, Entity entity) {
    return builder(uri, path).put(entity);
  }

  protected Response put(String uri, String path, Object entity) {
    return put(uri, path, Entity.entity(entity, APPLICATION_JSON_TYPE));
  }

  protected Response delete(String uri, String path) {
    return builder(uri, path).delete();
  }

  protected Response get(String uri, String path) {
    return builder(uri, path).get();
  }

  protected <T> T get(String uri, String path, Class<T> clazz) {
    return get(uri, path).readEntity(clazz);
  }

  protected Response patch(String uri, String path, Entity entity) {
    Invocation.Builder builder = builder(uri, path);
    builder.property(SET_METHOD_WORKAROUND, true);
    return builder.method("PATCH", entity);
  }

  private Invocation.Builder builder(String uri, String path) {
    WebTarget target = getTarget(uri, path);
    Invocation.Builder builder = target.request(APPLICATION_JSON_TYPE).header(USER_AGENT, getUserAgent());
    configureRequest(builder);
    return builder;
  }

  protected WebTarget getTarget(String uri, String path) {
    ClientBuilder builder = ClientBuilder.newBuilder();
    configureSecurityContext(builder);
    Client client = builder.build().register(MultiPartFeature.class);
    if (log != null && log.isDebugEnabled() && !isLoginRequest(path)) {
      client.register(new ClientLoggingFilter(log));
    }

    return client.target(uri).path(path);
  }

  private boolean isLoginRequest(String path) {
    return LOGIN.equals(path);
  }

  protected String getUserAgent() {
    Package classPackage = AbstractClient.class.getPackage();
    String implementationVersion = classPackage != null ? classPackage.getImplementationVersion() : EMPTY;

    String version = isNotBlank(implementationVersion) ? "-[" + implementationVersion + "]" : EMPTY;
    return String.format(USER_AGENT_MULE_DEPLOYER, version);
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

  protected void checkResponseStatus(Response response, Status... successStatus) {
    // TODO yes it can be done with a lambda but after cp to 2.x
    List<Integer> success = new ArrayList<>();
    for (Status s : successStatus) {
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

}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.arm;

import org.mule.tools.client.AbstractMuleClient;
import org.mule.tools.client.arm.model.*;
import org.mule.tools.client.model.TargetType;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.utils.DeployerLog;

public class ArmClient extends AbstractMuleClient {

  private static final String APPLICATIONS = "/hybrid/api/v1/applications";
  private static final String SERVERS = "/hybrid/api/v1/servers";
  private static final String SERVER_GROUPS = "/hybrid/api/v1/serverGroups";
  private static final String CLUSTERS = "/hybrid/api/v1/clusters";
  private boolean armInsecure;

  public ArmClient(ArmDeployment armDeployment, DeployerLog log) {
    super(armDeployment, log);
    armInsecure = armDeployment.isArmInsecure().get();
    if (armInsecure) {
      log.warn("Using insecure mode for connecting to ARM, please consider configuring your truststore with ARM certificates. This option is insecure and not intended for production use.");
    }
  }

  public Boolean isStarted(int applicationId) {
    Application application = getApplicationStatus(applicationId);
    return "STARTED".equals(application.data.lastReportedStatus);
  }

  public Application getApplicationStatus(int applicationId) {
    return get(uri, APPLICATIONS + "/" + applicationId, Application.class);
  }

  public String undeployApplication(int applicationId) {
    Response response = delete(uri, APPLICATIONS + "/" + applicationId);
    validateStatusSuccess(response);
    return response.readEntity(String.class);
  }

  public String undeployApplication(String appName, TargetType targetType, String target) {
    Integer applicationId = findApplication(appName, targetType, target);
    if (applicationId == null) {
      String appNotFoundMessage = "Application %s does not exist on %s %s.";
      throw new NotFoundException(String.format(appNotFoundMessage, appName, targetType.toString(), target));
    }
    return undeployApplication(applicationId);
  }

  public Application deployApplication(File app, String appName, TargetType targetType, String target) {
    MultiPart body = buildRequestBody(app, appName, targetType, target);
    Response response = post(uri, APPLICATIONS, Entity.entity(body, body.getMediaType()));
    validateStatusSuccess(response);
    return response.readEntity(Application.class);
  }

  public Application redeployApplication(int applicationId, File app, String appName, TargetType targetType, String target) {
    MultiPart body = buildRequestBody(app, appName, targetType, target);
    Response response = patch(uri, APPLICATIONS + "/" + applicationId, Entity.entity(body, body.getMediaType()));
    validateStatusSuccess(response);
    return response.readEntity(Application.class);
  }

  private MultiPart buildRequestBody(File app, String appName, TargetType targetType, String target) {
    String id = getId(targetType, target);
    FileDataBodyPart applicationPart = new FileDataBodyPart("file", app);
    MultiPart body = new FormDataMultiPart()
        .field("artifactName", appName)
        .field("targetId", id)
        .bodyPart(applicationPart);
    return body;
  }

  private String getId(TargetType targetType, String target) {
    String id = null;
    switch (targetType) {
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

  public Applications getApplications() {
    return get(uri, APPLICATIONS, Applications.class);
  }

  public Target findServerByName(String name) {
    return findTargetByName(name, SERVERS);
  }

  public Target findServerGroupByName(String name) {
    return findTargetByName(name, SERVER_GROUPS);
  }

  public Target findClusterByName(String name) {
    return findTargetByName(name, CLUSTERS);
  }

  private Target findTargetByName(String name, String path) {
    Targets response = get(uri, path, Targets.class);
    if (response.data == null) // Workaround because an empty array in the response is mapped as null
    {
      throw new RuntimeException("Couldn't find target named [" + name + "]");
    }
    for (int i = 0; i < response.data.length; i++) {
      if (name.equals(response.data[i].name)) {
        return response.data[i];
      }
    }
    throw new RuntimeException("Couldn't find target named [" + name + "]");
  }

  public Integer findApplication(String name, TargetType targetType, String target) {
    Applications apps = getApplications();
    Data[] appArray = getApplications().data;
    if (appArray == null) {
      return null;
    }
    String targetId = getId(targetType, target);
    for (int i = 0; i < appArray.length; i++) {
      if (name.equals(appArray[i].artifact.name) && targetId.equals(appArray[i].target.id)) {
        return appArray[i].id;
      }
    }
    return null;
  }

  protected void configureSecurityContext(ClientBuilder builder) {
    if (armInsecure) {
      try {
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, new TrustManager[] {new TrustAllManager()}, new java.security.SecureRandom());
        builder.hostnameVerifier(new DummyHostnameVerifier()).sslContext(sslcontext);
      } catch (KeyManagementException | NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }

    }
  }

  private static class DummyHostnameVerifier implements HostnameVerifier {

    public boolean verify(String s, SSLSession sslSession) {
      return true;
    }
  };

  private static class TrustAllManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  };

}

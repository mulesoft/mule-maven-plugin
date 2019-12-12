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

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.mule.tools.client.AbstractMuleClient;
import org.mule.tools.client.arm.model.Application;
import org.mule.tools.client.arm.model.Applications;
import org.mule.tools.client.arm.model.Data;
import org.mule.tools.client.arm.model.RegistrationToken;
import org.mule.tools.client.arm.model.Servers;
import org.mule.tools.client.arm.model.Target;
import org.mule.tools.client.arm.model.Targets;
import org.mule.tools.client.model.TargetType;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.AnypointDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.utils.DeployerLog;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class ArmClient extends AbstractMuleClient {

  private static final String API_VERSION = "v1";
  public static final String BASE_HYBRID_API_PATH = "/hybrid/api";
  public static final String HYBRID_API_V1 = BASE_HYBRID_API_PATH + "/" + API_VERSION;
  private static final String CLUSTERS = HYBRID_API_V1 + "/clusters";
  private static final String APPLICATIONS = HYBRID_API_V1 + "/applications";
  private static final String SERVER_GROUPS = HYBRID_API_V1 + "/serverGroups";

  private static final String SERVERS = HYBRID_API_V1 + "/servers";
  private static final String SERVER_GROUP = HYBRID_API_V1 + "/serverGroups";
  private static final String REGISTRATION = HYBRID_API_V1 + "/servers/registrationToken";

  private static final String FAILED_STATUS = "FAILED";
  private static final String STARTED_STATUS = "STARTED";
  private static final String DEPLOYMENT_IN_PROGRESS = "UPDATED";

  private boolean armInsecure;

  public ArmClient(Deployment armDeployment, DeployerLog log) {
    super((AnypointDeployment) armDeployment, log);
    armInsecure = ((ArmDeployment) armDeployment).isArmInsecure().get();
    if (armInsecure) {
      log.warn("Using insecure mode for connecting to ARM, please consider configuring your truststore with ARM certificates. This option is insecure and not intended for production use.");
    }
  }

  public String getRegistrationToken() {
    RegistrationToken registrationToken = get(baseUri, REGISTRATION, RegistrationToken.class);
    return registrationToken.data;
  }

  public Boolean isStarted(int applicationId) {
    Application application = getApplication(applicationId);
    if (application != null) {
      if (StringUtils.equalsIgnoreCase(application.data.desiredStatus, DEPLOYMENT_IN_PROGRESS)) {
        return false;
      } else if (containsIgnoreCase(application.data.lastReportedStatus, FAILED_STATUS)) {
        throw new IllegalStateException("Deployment failed");
      }
      return equalsIgnoreCase(STARTED_STATUS, application.data.lastReportedStatus);
    }
    return false;
  }

  public Application getApplication(int applicationId) {
    return get(baseUri, APPLICATIONS + "/" + applicationId, Application.class);
  }

  public String undeployApplication(int applicationId) {
    Response response = delete(baseUri, APPLICATIONS + "/" + applicationId);
    checkResponseStatus(response);
    return response.readEntity(String.class);
  }

  public String undeployApplication(ApplicationMetadata applicationMetadata) {
    Integer applicationId = findApplicationId(applicationMetadata);
    if (applicationId == null) {
      throw new NotFoundException("The " + applicationMetadata.toString() + "does not exist.");
    }
    return undeployApplication(applicationId);
  }

  public Application deployApplication(ApplicationMetadata applicationMetadata) {
    MultiPart body = buildRequestBody(applicationMetadata);
    Response response = post(baseUri, APPLICATIONS, Entity.entity(body, body.getMediaType()));
    checkResponseStatus(response);
    return response.readEntity(Application.class);
  }

  public Application redeployApplication(int applicationId, ApplicationMetadata applicationMetadata) {
    MultiPart body = buildRequestBody(applicationMetadata);
    Response response = patch(baseUri, APPLICATIONS + "/" + applicationId, Entity.entity(body, body.getMediaType()));
    checkResponseStatus(response);
    return response.readEntity(Application.class);
  }

  private MultiPart buildRequestBody(ApplicationMetadata metadata) {
    return buildRequestBody(metadata.getFile(), metadata.getName(), metadata.getTargetType(), metadata.getTarget());
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

  public String getId(TargetType targetType, String target) {
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
    return get(baseUri, APPLICATIONS, Applications.class);
  }

  // TODO move servers and targets to another package due to the ugly ARM API
  public List<Target> getServers() {
    Targets targets = get(baseUri, SERVERS, Targets.class);
    return Arrays.asList(targets.data);
  }

  public Servers getServer(Integer serverId) {
    Servers target = get(baseUri, SERVERS + "/" + serverId, Servers.class);
    return target;
  }

  public Servers getServerGroup(Integer serverGroupId) {
    Servers target = get(baseUri, SERVER_GROUP + "/" + serverGroupId, Servers.class);
    return target;
  }

  public void deleteServer(Integer serverId) {
    Response response = delete(baseUri, SERVERS + "/" + serverId);
    checkResponseStatus(response);
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
    Targets response = get(baseUri, path, Targets.class);

    // Workaround because an empty array in the response is mapped as null
    if (response.data == null) {
      throw new RuntimeException("Couldn't find target named [" + name + "]");
    }

    for (int i = 0; i < response.data.length; i++) {
      if (name.equals(response.data[i].name)) {
        return response.data[i];
      }
    }
    throw new RuntimeException("Couldn't find target named [" + name + "]");
  }

  public Integer findApplicationId(ApplicationMetadata applicationMetadata) {
    Applications apps = getApplications();
    Data[] appArray = getApplications().data;
    if (appArray == null) {
      return null;
    }
    String targetId = getId(applicationMetadata.getTargetType(), applicationMetadata.getTarget());
    for (int i = 0; i < appArray.length; i++) {
      if (applicationMetadata.getName().equals(appArray[i].artifact.name) && targetId.equals(appArray[i].target.id)) {
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

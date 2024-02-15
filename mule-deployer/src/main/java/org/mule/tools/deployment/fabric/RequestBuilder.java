/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.client.fabric.model.ApplicationModify;
import org.mule.tools.client.fabric.model.ApplicationRequest;
import org.mule.tools.client.fabric.model.AssetReference;
import org.mule.tools.client.fabric.model.DeploymentGenericResponse;
import org.mule.tools.client.fabric.model.DeploymentModify;
import org.mule.tools.client.fabric.model.DeploymentRequest;
import org.mule.tools.client.fabric.model.Deployments;
import org.mule.tools.client.fabric.model.Target;
import org.mule.tools.model.anypoint.Runtime;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.model.anypoint.RuntimeFabricDeploymentSettings;
import org.mule.tools.model.anypoint.RuntimeFabricOnPremiseDeploymentSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class RequestBuilder {

  public static final String ID = "id";
  protected RuntimeFabricDeployment deployment;
  protected RuntimeFabricClient client;
  private static final String AGENT_INFO = "agentInfo";
  private static final String NAME = "name";
  private static final String DOMAIN_WILDCARD = "*";

  public RequestBuilder() {}

  public RequestBuilder(RuntimeFabricDeployment deployment, RuntimeFabricClient client) {
    this.deployment = deployment;
    this.client = client;
  }

  public DeploymentRequest buildDeploymentRequest() throws DeploymentException {
    ApplicationRequest applicationRequest = buildApplicationRequest();

    Target target = buildTarget();

    DeploymentRequest deploymentRequest = new DeploymentRequest();

    deploymentRequest.setName(deployment.getApplicationName());
    deploymentRequest.setApplication(applicationRequest);
    deploymentRequest.setTarget(target);

    Map<String, Object> applicationPropertiesService = new HashMap<>();
    Map<String, Object> properties = new HashMap<>();
    properties.put("properties", deployment.getProperties());
    properties.put("secureProperties", deployment.getSecureProperties());
    properties.put("applicationName", deployment.getApplicationName());
    applicationPropertiesService.put("mule.agent.application.properties.service", properties);
    applicationRequest.setConfiguration(applicationPropertiesService);

    return deploymentRequest;
  }

  public Target buildTarget() throws DeploymentException {
    Target target = new Target();
    target.setProvider(deployment.getProvider());
    target.setTargetId(resolveTargetId());
    target.setReplicas(deployment.getReplicas());
    RuntimeFabricDeploymentSettings resolvedDeploymentSettings =
        resolveDeploymentSettings(deployment.getDeploymentSettings());
    target.setDeploymentSettings(resolvedDeploymentSettings);
    return target;
  }

  protected RuntimeFabricDeploymentSettings resolveDeploymentSettings(RuntimeFabricDeploymentSettings settings)
      throws DeploymentException {
    RuntimeFabricDeploymentSettings resolvedDeploymentSettings =
        new RuntimeFabricOnPremiseDeploymentSettings((RuntimeFabricOnPremiseDeploymentSettings) settings);
    String targetId = resolveTargetId();

    if (Stream.of(deployment.getMuleVersion(), deployment.getJavaVersion(), deployment.getReleaseChannel())
        .anyMatch(Optional::isPresent)) {
      Runtime runtime = new Runtime();
      deployment.getMuleVersion().ifPresent(runtime::setVersion);
      deployment.getJavaVersion().ifPresent(runtime::setJava);
      deployment.getReleaseChannel().ifPresent(runtime::setReleaseChannel);
      resolvedDeploymentSettings.setRuntime(runtime);
    }

    deployment.getMuleVersion().ifPresent(resolvedDeploymentSettings::setRuntimeVersion);

    String url = resolveUrl(settings, targetId);

    resolvedDeploymentSettings.getHttp().getInbound().setPublicUrl(url);

    return resolvedDeploymentSettings;
  }

  protected String resolveUrl(RuntimeFabricDeploymentSettings deploymentSettings, String targetId) {
    JsonArray domains = client.getDomainInfo(targetId);
    if (deploymentSettings.getHttp().getInbound().getPublicUrl() == null && domains.size() > 0) {
      String domain = domains.get(0).getAsString();
      return domain.replace(DOMAIN_WILDCARD, deployment.getApplicationName());
    } else {
      return deploymentSettings.getHttp().getInbound().getPublicUrl();
    }
  }

  protected String resolveTag(String targetId, String muleVersion) {
    JsonObject targetInfo = client.getTargetInfo(targetId);
    if (targetInfo.has("runtimes")) {
      JsonArray runtimes = targetInfo.getAsJsonArray("runtimes");
      return getTag(runtimes, muleVersion);
    }
    return null;
  }

  private String getTag(JsonArray runtimes, String muleVersion) {
    for (int i = 0; i < runtimes.size(); i++) {
      JsonObject runtime = runtimes.get(i).getAsJsonObject();
      if (runtime.has("versions")) {
        JsonArray versions = runtime.getAsJsonArray("versions");
        for (int j = 0; j < versions.size(); j++) {
          JsonObject version = versions.get(j).getAsJsonObject();
          if (version.has("baseVersion") && version.has("tag")) {
            if (StringUtils.equals(version.get("baseVersion").getAsString(), muleVersion)) {
              return version.get("tag").getAsString();
            }
          }
        }
      }
    }
    return null;
  }

  protected String resolveTargetId() throws DeploymentException {
    String targetName = deployment.getTarget();
    JsonArray targets = client.getTargets();
    return getTargetId(targets, targetName);
  }

  public static String getTargetId(JsonArray targets, String targetName) throws DeploymentException {
    for (JsonElement targetElement : targets) {
      JsonObject target = targetElement.getAsJsonObject();
      if (target != null) {
        JsonElement nameElement = target.get(NAME);
        if (nameElement != null) {
          String currentTargetName = nameElement.getAsString();
          if (StringUtils.equals(targetName, currentTargetName)) {
            return target.get(ID).getAsString();
          }
        }
      }
    }
    throw new DeploymentException("Could not find target " + targetName);
  }

  protected ApplicationRequest buildApplicationRequest() {
    AssetReference assetReference = buildAssetReference();
    ApplicationRequest applicationRequest = new ApplicationRequest();
    applicationRequest.setRef(assetReference);
    applicationRequest.setDesiredState("STARTED");
    return applicationRequest;
  }

  protected AssetReference buildAssetReference() {
    AssetReference assetReference = new AssetReference();

    assetReference.setArtifactId(deployment.getArtifactId());
    assetReference.setGroupId(deployment.getGroupId());
    assetReference.setVersion(deployment.getVersion());
    assetReference.setPackaging("jar");

    return assetReference;
  }

  public String getDeploymentId(Target target) {
    Deployments deployments = client.getDeployments();
    for (DeploymentGenericResponse deployment : deployments) {
      if (StringUtils.equals(deployment.name, this.deployment.getApplicationName()) &&
          StringUtils.equals(deployment.target.targetId, target.targetId)) {
        return deployment.id;
      }
    }
    throw new IllegalStateException("Could not find deployment ID. Please check if there is an application with the same name under a different environment.");
  }

  public DeploymentModify buildDeploymentModify() throws DeploymentException {
    Target target = buildTarget();

    ApplicationModify applicationModify = buildApplicationModify();

    DeploymentModify deploymentModify = new DeploymentModify();
    deploymentModify.setTarget(target);
    deploymentModify.setApplication(applicationModify);

    return deploymentModify;
  }

  protected ApplicationModify buildApplicationModify() {
    AssetReference assetReference = buildAssetReference();
    ApplicationModify applicationModify = new ApplicationModify();
    applicationModify.setRef(assetReference);

    if (deployment.getProperties() != null) {
      Map<String, Object> applicationPropertiesService = new HashMap<>();
      Map<String, Object> properties = new HashMap<>();
      properties.put("properties", deployment.getProperties());
      properties.put("secureProperties", deployment.getSecureProperties());
      properties.put("applicationName", deployment.getApplicationName());
      applicationPropertiesService.put("mule.agent.application.properties.service", properties);

      applicationModify.setConfiguration(applicationPropertiesService);
    }

    return applicationModify;
  }
}

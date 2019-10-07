/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.fabric;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.model.anypoint.RuntimeFabricDeploymentSettings;
import org.mule.tools.utils.PropertiesUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RequestBuilder {

  public static final String ID = "id";
  private RuntimeFabricDeployment deployment;
  private RuntimeFabricClient client;
  private static final String AGENT_INFO = "agentInfo";
  private static final String NAME = "name";
  private static final String DOMAIN_WILDCARD = "*";

  protected RequestBuilder(RuntimeFabricDeployment deployment, RuntimeFabricClient client) {
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
    try {
      properties.put("properties",
                     PropertiesUtils.resolvePropertiesFromFile(deployment.getProperties(),
                                                               deployment.getPropertiesFile(), false));
    } catch (IOException e) {
      throw new DeploymentException(e.getMessage(), e);
    }
    properties.put("secureproperties", new HashMap<String, String>());
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
    RuntimeFabricDeploymentSettings resolvedMuleVersionDeploymentSettings =
        resolveMuleVersionDeploymentSettings(deployment.getDeploymentSettings());
    target.setDeploymentSettings(resolvedMuleVersionDeploymentSettings);
    return target;
  }

  private RuntimeFabricDeploymentSettings resolveMuleVersionDeploymentSettings(RuntimeFabricDeploymentSettings settings)
      throws DeploymentException {
    RuntimeFabricDeploymentSettings resolvedMuleVersionDeploymentSettings = new RuntimeFabricDeploymentSettings(settings);
    String targetId = resolveTargetId();
    String muleVersion = deployment.getMuleVersion();
    String tag = resolveTag(targetId, muleVersion);
    if (tag != null) {
      resolvedMuleVersionDeploymentSettings.setRuntimeVersion(muleVersion + ":" + tag);
    } else {
      throw new DeploymentException("Could not resolve tag for this mule version");
    }
    String url = resolveUrl(settings, targetId);

    resolvedMuleVersionDeploymentSettings.getHttp().getInbound().setPublicUrl(url);


    return resolvedMuleVersionDeploymentSettings;
  }

  private String resolveUrl(RuntimeFabricDeploymentSettings deploymentSettings, String targetId) {
    JsonArray domains = client.getDomainInfo(targetId);
    if (deploymentSettings.getHttp().getInbound().getPublicUrl() == null && domains.size() > 0) {
      String domain = domains.get(0).getAsString();
      return domain.replace(DOMAIN_WILDCARD, deployment.getApplicationName());
    } else {
      return deploymentSettings.getHttp().getInbound().getPublicUrl();
    }
  }

  private String resolveTag(String targetId, String muleVersion) {
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

  private String resolveTargetId() throws DeploymentException {
    String targetName = deployment.getTarget();
    JsonArray targets = client.getTargets();
    return getTargetId(targets, targetName);
  }

  private String getTargetId(JsonArray targets, String targetName) throws DeploymentException {
    for (JsonElement targetElement : targets) {
      JsonObject target = targetElement.getAsJsonObject();
      if (target != null) {
        JsonElement agentInfoElement = target.get(AGENT_INFO);
        if (agentInfoElement != null) {
          JsonObject agentInfo = agentInfoElement.getAsJsonObject();
          JsonElement nameElement = agentInfo.get(NAME);
          if (nameElement != null) {
            String currentTargetName = nameElement.getAsString();
            if (StringUtils.equals(targetName, currentTargetName)) {
              return target.get(ID).getAsString();
            }
          }
        }
      }
    }
    throw new DeploymentException("Could not find target " + deployment.getTarget());
  }

  private ApplicationRequest buildApplicationRequest() {
    AssetReference assetReference = buildAssetReference();
    ApplicationRequest applicationRequest = new ApplicationRequest();
    applicationRequest.setRef(assetReference);
    applicationRequest.setDesiredState("STARTED");
    return applicationRequest;
  }

  private AssetReference buildAssetReference() {
    AssetReference assetReference = new AssetReference();

    assetReference.setArtifactId(deployment.getArtifactId());
    assetReference.setGroupId(deployment.getGroupId());
    assetReference.setVersion(deployment.getVersion());
    assetReference.setPackaging("zip");

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
    throw new IllegalStateException("Could not find deployment ID.");
  }

  public DeploymentModify buildDeploymentModify() throws DeploymentException {

    Target target = buildTarget();

    ApplicationModify applicationModify = buildApplicationModify();

    DeploymentModify deploymentModify = new DeploymentModify();
    deploymentModify.setTarget(target);
    deploymentModify.setApplication(applicationModify);

    return deploymentModify;
  }

  private ApplicationModify buildApplicationModify() throws DeploymentException {
    AssetReference assetReference = buildAssetReference();
    ApplicationModify applicationModify = new ApplicationModify();
    applicationModify.setRef(assetReference);

    if (deployment.getProperties() != null || deployment.getPropertiesFile() != null) {
      Map<String, Object> applicationPropertiesService = new HashMap<>();
      Map<String, Object> properties = new HashMap<>();
      try {
        properties.put("properties",
                       PropertiesUtils.resolvePropertiesFromFile(deployment.getProperties(), deployment.getPropertiesFile(),
                                                                 false));
      } catch (IOException e) {
        throw new DeploymentException(e.getMessage(), e);
      }
      properties.put("applicationName", deployment.getApplicationName());
      applicationPropertiesService.put("mule.agent.application.properties.service", properties);

      applicationModify.setConfiguration(applicationPropertiesService);
    }

    return applicationModify;
  }
}

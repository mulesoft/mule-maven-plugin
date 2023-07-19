/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.cloudhub2;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Cloudhub2Configuration {

  @SerializedName("mule.agent.application.properties.service")
  private Object applicationPropertiesService;

  @SerializedName("mule.agent.logging.service")
  private Object loggingService;

  public Cloudhub2Configuration(Map<String, Object> applicationPropertiesService, Map<String, Object> loggingService) {
    this.applicationPropertiesService = applicationPropertiesService;
    this.loggingService = loggingService;
  }

}

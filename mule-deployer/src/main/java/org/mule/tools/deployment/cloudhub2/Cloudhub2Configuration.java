/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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

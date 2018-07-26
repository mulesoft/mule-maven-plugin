/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.project.policy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class PolicyYaml {

  @JsonCreator
  public PolicyYaml(
                    @JsonProperty(value = "id", required = true) String id,
                    @JsonProperty(value = "name", required = true) String name,
                    @JsonProperty(value = "supportedPoliciesVersions", required = true) String supportedPoliciesVersions,
                    @JsonProperty(value = "description", required = true) String description,
                    @JsonProperty(value = "category", required = true) String category,
                    @JsonProperty(value = "type", required = true) String type,
                    @JsonProperty(value = "resourceLevelSupported", required = true) Boolean resourceLevelSupported,
                    @JsonProperty(value = "standalone", required = true) Boolean standalone,
                    @JsonProperty(value = "requiredCharacteristics", required = true) List<String> requiredCharacteristics,
                    @JsonProperty(value = "providedCharacteristics", required = true) List<String> providedCharacteristics,
                    @JsonProperty(value = "configuration", required = true) List<ConfigurationProperty> configuration) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ConfigurationProperty {

    @JsonCreator
    public ConfigurationProperty(
                                 @JsonProperty(value = "propertyName", required = true) String propertyName,
                                 @JsonProperty(value = "type", required = true) String type) {}
  }
}

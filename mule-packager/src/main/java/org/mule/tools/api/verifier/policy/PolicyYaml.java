/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.verifier.policy;

import java.util.List;

public class PolicyYaml {

  public String id;
  public String name;
  public String description;
  public String category;
  public String type;
  public Boolean resourceLevelSupported;
  public Boolean standalone;
  public List<String> requiredCharacteristics;
  public List<String> providedCharacteristics;
  public List<ConfigurationProperty> configuration;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Boolean getResourceLevelSupported() {
    return resourceLevelSupported;
  }

  public void setResourceLevelSupported(Boolean resourceLevelSupported) {
    this.resourceLevelSupported = resourceLevelSupported;
  }

  public Boolean getStandalone() {
    return standalone;
  }

  public void setStandalone(Boolean standalone) {
    this.standalone = standalone;
  }

  public List<String> getRequiredCharacteristics() {
    return requiredCharacteristics;
  }

  public void setRequiredCharacteristics(List<String> requiredCharacteristics) {
    this.requiredCharacteristics = requiredCharacteristics;
  }

  public List<String> getProvidedCharacteristics() {
    return providedCharacteristics;
  }

  public void setProvidedCharacteristics(List<String> providedCharacteristics) {
    this.providedCharacteristics = providedCharacteristics;
  }

  public List<ConfigurationProperty> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(List<ConfigurationProperty> configuration) {
    this.configuration = configuration;
  }


}

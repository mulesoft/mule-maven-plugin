/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.fabric.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class ApplicationConfiguration {

  @XmlElement(name = "mule.agent.application.properties.service")
  public MuleAgentApplicationPropertiesService muleAgentApplicationPropertiesService;

  public void setMuleAgentApplicationPropertiesService(MuleAgentApplicationPropertiesService muleAgentApplicationPropertiesService) {
    this.muleAgentApplicationPropertiesService = muleAgentApplicationPropertiesService;
  }
}

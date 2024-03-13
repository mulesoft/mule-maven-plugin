/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.fabric.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MuleAgentApplicationPropertiesService {

  public String applicationName;

  @XmlElement(name = "properties")
  @XmlJavaTypeAdapter(PropertiesAdapter.class)
  public Map<String, String> properties;

  @XmlElement(name = "secureProperties")
  @XmlJavaTypeAdapter(PropertiesAdapter.class)
  public Map<String, String> secureProperties;
}

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller.probing;

import org.apache.commons.lang3.StringUtils;

import static org.mule.tools.api.classloader.model.Artifact.MULE_DOMAIN;

public class ProbeFactory {

  public static DeploymentProbe createProbe(String packaging) {
    if (StringUtils.equals(packaging, MULE_DOMAIN)) {
      return new DomainDeploymentProbe();
    } else {
      return new AppDeploymentProbe();
    }
  }
}

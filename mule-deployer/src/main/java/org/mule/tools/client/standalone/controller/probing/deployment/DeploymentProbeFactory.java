/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.standalone.controller.probing.deployment;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.core.exception.DeploymentException;


public class DeploymentProbeFactory {

  private static final String MULE_DOMAIN_PACKAGING = "mule-domain";
  private static final String MULE_APPLICATION_PACKAGING = "mule-application";

  /**
   * Deploys the application.
   * 
   * @param packaging Given a packaging, creates the corresponding implementation of a
   *        {@link org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbe}. Currently, it supports
   *        mule-domain and mule-application probes.
   */
  public static DeploymentProbe createProbe(String packaging) throws DeploymentException {
    if (StringUtils.equals(packaging, MULE_DOMAIN_PACKAGING)) {
      return new DomainDeploymentProbe();
    } else if (StringUtils.equals(packaging, MULE_APPLICATION_PACKAGING)) {
      return new ApplicationDeploymentProbe();
    }
    throw new DeploymentException("Packaging " + packaging + " has no probe support");
  }
}

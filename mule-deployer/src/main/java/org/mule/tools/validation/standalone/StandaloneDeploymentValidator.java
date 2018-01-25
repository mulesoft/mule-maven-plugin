/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.standalone;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.walkFileTree;

/**
 * Validates if the mule runtime version is valid in a Standalone deployment scenario.
 */
public class StandaloneDeploymentValidator extends AbstractDeploymentValidator {

  public StandaloneDeploymentValidator(Deployment deployment) {
    super(deployment);
  }

  @Override
  public EnvironmentSupportedVersions getEnvironmentSupportedVersions() throws DeploymentException {
    File muleHome = ((StandaloneDeployment) deployment).getMuleHome();

    checkArgument(muleHome != null, "Mule home cannot not be null");
    checkArgument(muleHome.exists(), "Mule home directory does not exist");

    String muleRuntimeVersion = findRuntimeVersion(muleHome);
    return new EnvironmentSupportedVersions(muleRuntimeVersion);
  }

  /**
   * Retrieves the mule runtime version in a standalone distribution.
   * 
   * @param muleHome the mule home of the distribution.
   * @return the mule runtime version of the distribution.
   * @throws DeploymentException if cannot find the mule core version.
   */
  private String findRuntimeVersion(File muleHome) throws DeploymentException {
    MuleCoreJarVersionFinder muleCoreJarVersionFinder = new MuleCoreJarVersionFinder();
    try {
      walkFileTree(muleHome.toPath(), muleCoreJarVersionFinder);
    } catch (IOException e) {
      throw new DeploymentException("Error trying to resolve Mule Standalone version", e);
    }
    return muleCoreJarVersionFinder.getMuleCoreVersion();
  }
}

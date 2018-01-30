/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.deployment;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.utils.DeployerLog;

/**
 *
 */
public class StandaloneUndeployer {

  private List<File> muleHomes;
  private String applicationName;
  private DeployerLog log;

  public StandaloneUndeployer(DeployerLog log, String applicationName, File... muleHomes) {
    this.muleHomes = Arrays.asList(muleHomes);
    this.applicationName = applicationName;
    this.log = log;
  }

  public void execute() throws DeploymentException {
    for (File muleHome : muleHomes) {
      log.info("Undeploying application " + applicationName + " from " + muleHome.getAbsolutePath());
      undeploy(muleHome);
      log.info("Application " + applicationName + " undeployed");

      log.info("Stopping Mule instance " + muleHome.getAbsolutePath());
      MuleProcessController controller = new MuleProcessController(muleHome.getAbsolutePath());
      controller.stop();
      log.info("Mule instance stopped");
    }
  }

  private void undeploy(File muleHome) throws DeploymentException {
    File appsDir = new File(muleHome + "/apps/");

    for (File file : appsDir.listFiles()) {
      if (FilenameUtils.getBaseName(file.getName()).equals(applicationName)) {
        try {
          log.debug("Deleting " + file);
          FileUtils.forceDelete(file);
          return;
        } catch (IOException e) {
          log.error("Could not delete " + file.getAbsolutePath());
          throw new DeploymentException("Could not delete directory [" + file.getAbsolutePath() + "]", e);
        }
      }
    }

    throw new DeploymentException("Application " + applicationName + " not found.");
  }
}

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.configuration;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.core.exception.DeploymentException;

public class ClusterConfigurator {

  public boolean configureCluster(File[] paths, List<MuleProcessController> mules) throws DeploymentException {
    int nodeNumber = 1;
    for (File f : paths) {
      try {
        if (!new File(f.getAbsolutePath() + "/.mule").mkdirs()) {
          throw new DeploymentException("Couldn't create .mule dir at: " + f.getAbsolutePath());
        }
        createClusterConfig(paths, mules, nodeNumber, f);
        nodeNumber++;
      } catch (IOException ex) {
        throw new DeploymentException("Couldn't create mule-cluster.properties in one of the mules" + ex.getMessage());
      }

    }
    return true;
  }

  private void createClusterConfig(File[] paths, List<MuleProcessController> mules, int nodeNumber, File f)
      throws IOException, DeploymentException {
    BufferedWriter writer = null;
    try {

      writer = new BufferedWriter(new OutputStreamWriter(
                                                         new FileOutputStream(f.getAbsolutePath()
                                                             + "/.mule/mule-cluster.properties"),
                                                         StandardCharsets.UTF_8));
      writer.write("mule.clusterSize=" + paths.length);
      writer.newLine();
      writer.write("mule.clusterSchema=partitioned-sync2backup");
      writer.newLine();
      writer.write("mule.clusterId=" + mules.hashCode());
      writer.newLine();
      writer.write("mule.clusterNodeId=" + nodeNumber);
      writer.close();
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        throw new DeploymentException("Couldn't close mule-cluster.properties file: " + f.getAbsolutePath());
      }
    }
  }

}

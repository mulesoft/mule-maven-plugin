/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;

import org.mule.test.infrastructure.process.MuleProcessController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;

public class ClusterConfigurator {

  public boolean configureCluster(File[] paths, List<MuleProcessController> mules) throws MojoFailureException {
    int nodeNumber = 1;
    for (File f : paths) {
      try {
        if (!new File(f.getAbsolutePath() + "/.mule").mkdirs()) {
          throw new MojoFailureException("Couldn't create .mule dir at: " + f.getAbsolutePath());
        }
        createClusterConfig(paths, mules, nodeNumber, f);
        nodeNumber++;
      } catch (IOException ex) {
        throw new MojoFailureException("Couldn't create mule-cluster.properties in one of the mules" + ex.getMessage());
      }

    }
    return true;
  }

  private void createClusterConfig(File[] paths, List<MuleProcessController> mules, int nodeNumber, File f)
      throws IOException, MojoFailureException {
    BufferedWriter writer = null;
    try {

      writer = new BufferedWriter(new OutputStreamWriter(
                                                         new FileOutputStream(f.getAbsolutePath()
                                                             + "/.mule/mule-cluster.properties"),
                                                         StandardCharsets.UTF_8));
      writer.write("mule.clusterSize=" + paths.length);
      writer.write("mule.clusterSchema=partitioned-sync2backup");
      writer.write("mule.clusterId=" + mules.hashCode());
      writer.write("mule.clusterNodeId=" + nodeNumber);
      writer.close();
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        throw new MojoFailureException("Couldn't close mule-cluster.properties file: " + f.getAbsolutePath());
      }
    }
  }

}

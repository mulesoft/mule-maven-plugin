/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ControllerTest {

  @TempDir
  public File temporaryFolder;

  @Test
  public void testController() throws IOException {
    temporaryFolder.toPath().resolve("conf").toFile().mkdirs();
    temporaryFolder.toPath().resolve("lib").resolve("user").toFile().mkdirs();
    temporaryFolder.toPath().resolve("conf").resolve("wrapper.conf").toFile().createNewFile();
    temporaryFolder.toPath().resolve("mule-app").toFile().createNewFile();
    temporaryFolder.toPath().resolve("lib.jar").toFile().createNewFile();
    BufferedWriter writer =
        new BufferedWriter(new FileWriter(temporaryFolder.toPath().resolve("conf").resolve("wrapper.conf").toFile()));
    writer.write("wrapper.java.additional.19=-Danypoint.platform.proxy_port=80");
    writer.close();
    AbstractOSController abstractOSController = new WindowsController(temporaryFolder.getAbsolutePath(), 20000);
    Controller controller = new Controller(abstractOSController, temporaryFolder.getAbsolutePath());
    controller.addConfProperty("2");
    controller.deploy(temporaryFolder.toPath().resolve("mule-app").toString());
    controller.deployDomain(temporaryFolder.toPath().resolve("mule-app").toString());
    controller.addLibrary(temporaryFolder.toPath().resolve("lib.jar").toFile());
  }
}

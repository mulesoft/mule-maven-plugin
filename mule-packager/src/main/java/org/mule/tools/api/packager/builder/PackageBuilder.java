/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.builder;


import org.codehaus.plexus.archiver.ArchiverException;

import java.io.File;
import java.io.IOException;

/**
 * Builder for packages.
 */
public interface PackageBuilder {

  /**
   * Cretes the package.
   * @param destinationFile file to be created with the content of the package.
   * @param originFolder location containing the resources that are going to be shipped in the package. It is expected that the
   *        folder structure in this location is going to have the same structure of the contents of the generated package.
   * @return
   */
  void createPackage(File destinationFile, String originFolder)
      throws ArchiverException, IOException;

  void createDeployableFile() throws IOException;
}
